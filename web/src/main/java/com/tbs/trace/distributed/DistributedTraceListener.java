package com.tbs.trace.distributed;

import com.tbs.trace.config.TraceProperty;
import com.tbs.trace.core.AbstractTraceContext;
import com.tbs.trace.core.IMethodInvokeTreeProvider;
import com.tbs.trace.core.MethodTraceObject;
import com.tbs.trace.core.TraceObject;
import com.tbs.trace.tool.ITraceContextListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 分布式链路追踪监听器
 * <p>
 * 实现ITraceContextListener，在请求链路结束时将本地trace树
 * 转换为可序列化的TraceTreeFragment，通过ZooKeeper存储到分布式trace节点
 * </p>
 *
 * @author abstergo
 */
@Slf4j
public class DistributedTraceListener implements ITraceContextListener {

    @Resource
    private DistributedTraceAggregator distributedTraceAggregator;

    @Resource
    private TraceProperty traceProperty;

    @Value("${spring.application.name:unknown}")
    private String serviceName;

    @Override
    public void onTraceContextChanged(int changeType, AbstractTraceContext traceContext,
        HttpServletRequest request, HttpServletResponse response) {
        // 仅在上下文销毁时采集trace数据
        if (changeType != CONTEXT_CHANGE_TYPE_CONTEXT_DESTROYED) {
            return;
        }

        // 检查分布式trace是否启用
        if (!traceProperty.isDubboTraceEnabled()) {
            return;
        }

        TraceObject rootTrace = traceContext.getRootTrace();
        if (rootTrace == null) {
            return;
        }

        try {
            // 提取本地trace树为可序列化的TraceNode树
            TraceNode rootNode = extractTraceTree(traceContext);
            if (rootNode == null) {
                return;
            }

            // 确定parentTraceId：根节点的parentTraceId（从RpcContext获取的挂载位置）
            String parentTraceId = rootTrace.getParentTraceId();

            // 构建片段
            TraceTreeFragment fragment = new TraceTreeFragment(
                rootTrace.getTraceId(), parentTraceId, serviceName, rootNode);

            // 通过ZooKeeper发布
            distributedTraceAggregator.publish(fragment);
        } catch (Exception e) {
            log.debug("分布式trace数据采集异常", e);
        }
    }

    @Override
    public void onMethodInvokeBegin(TraceObject traceObject, IMethodInvokeTreeProvider provider) {
        // 方法调用开始时不做处理，在结束时统一采集
    }

    @Override
    public void onMethodInvokeEnd(TraceObject traceObject, IMethodInvokeTreeProvider provider) {
        // 方法调用结束时不做处理，在上下文销毁时统一采集
    }

    /**
     * 从trace上下文中提取可序列化的trace树
     */
    private TraceNode extractTraceTree(AbstractTraceContext traceContext) {
        TraceObject rootTrace = traceContext.getRootTrace();
        if (rootTrace == null) {
            return null;
        }

        return buildTraceNode(rootTrace, traceContext);
    }

    /**
     * 递归构建TraceNode
     */
    private TraceNode buildTraceNode(TraceObject traceObject, AbstractTraceContext traceContext) {
        if (traceObject == null) {
            return null;
        }

        TraceNode node = new TraceNode(traceObject.getTraceId(), traceObject.getParentTraceId());
        node.setAvailableTime(traceObject.getAvailableTime());
        node.setDisposeTime(traceObject.getDisposeTime());

        // 如果是MethodTraceObject，提取方法信息
        if (traceObject instanceof MethodTraceObject) {
            MethodTraceObject methodTrace = (MethodTraceObject) traceObject;
            Method method = methodTrace.getMethod();
            if (method != null) {
                node.setMethodSignature(method.getName());
                node.setInterfaceName(method.getDeclaringClass().getSimpleName());
            }

            // 检测是否为Dubbo调用节点（接口类名不以tbs开头的外部服务调用）
            if (method != null) {
                String className = method.getDeclaringClass().getName();
                node.setDubboCall(!className.startsWith("com.tbs."));
            }
        }

        // 从树中查找子节点
        IMethodInvokeTreeProvider treeProvider = traceContext.getMethodInvokeTreeProvider();
        var treeNodeOpt = treeProvider.getTree().find(traceObject.getTraceId());
        if (treeNodeOpt.isPresent()) {
            List<com.tbs.struct.tree.api.ITreeNode<String, TraceObject>> children =
                treeNodeOpt.get().getChildren();
            if (children != null) {
                for (var child : children) {
                    TraceNode childTraceNode = buildTraceNode(child.getValue(), traceContext);
                    if (childTraceNode != null) {
                        node.addChild(childTraceNode);
                    }
                }
            }
        }

        return node;
    }
}
