package com.tbs.trace.distributed;

import com.tbs.trace.config.TraceProperty;
import com.tbs.trace.core.AbstractTraceContext;
import com.tbs.trace.core.IMethodInvokeTreeProvider;
import com.tbs.trace.core.MethodTraceObject;
import com.tbs.trace.core.TraceObject;
import com.tbs.trace.tool.ITraceContextListener;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 分布式链路追踪监听器
 * <p>
 * 实现ITraceContextListener，在请求链路结束时将本地trace树
 * 转换为可序列化的TraceTreeFragment，异步发布到分布式存储。
 * </p>
 *
 * @author abstergo
 */
@Slf4j
public class DistributedTraceListener implements ITraceContextListener {

    private final DistributedTraceAggregator distributedTraceAggregator;
    private final TraceProperty traceProperty;
    private final String serviceName;

    public DistributedTraceListener(DistributedTraceAggregator distributedTraceAggregator,
        TraceProperty traceProperty, String serviceName) {
        this.distributedTraceAggregator = distributedTraceAggregator;
        this.traceProperty = traceProperty;
        this.serviceName = serviceName;
    }

    @Override
    public void onTraceContextChanged(int changeType, AbstractTraceContext traceContext,
        HttpServletRequest request, HttpServletResponse response) {
        if (changeType != CONTEXT_CHANGE_TYPE_CONTEXT_DESTROYED) {
            return;
        }

        if (!traceProperty.isDubboTraceEnabled()) {
            return;
        }

        TraceObject rootTrace = traceContext.getRootTrace();
        if (rootTrace == null) {
            return;
        }

        try {
            TraceNode rootNode = extractTraceTree(traceContext);
            if (rootNode == null) {
                return;
            }

            String parentTraceId = rootTrace.getParentTraceId();
            TraceTreeFragment fragment = new TraceTreeFragment(
                rootTrace.getTraceId(), parentTraceId, serviceName, rootNode);

            distributedTraceAggregator.publish(fragment);
        } catch (Exception e) {
            log.debug("分布式trace数据采集异常", e);
        }
    }

    @Override
    public void onMethodInvokeBegin(TraceObject traceObject, IMethodInvokeTreeProvider provider) {
    }

    @Override
    public void onMethodInvokeEnd(TraceObject traceObject, IMethodInvokeTreeProvider provider) {
    }

    private TraceNode extractTraceTree(AbstractTraceContext traceContext) {
        TraceObject rootTrace = traceContext.getRootTrace();
        if (rootTrace == null) {
            return null;
        }
        return buildTraceNode(rootTrace, traceContext);
    }

    private TraceNode buildTraceNode(TraceObject traceObject, AbstractTraceContext traceContext) {
        if (traceObject == null) {
            return null;
        }

        TraceNode node = new TraceNode(traceObject.getTraceId(), traceObject.getParentTraceId());
        node.setAvailableTime(traceObject.getAvailableTime());
        node.setDisposeTime(traceObject.getDisposeTime());

        if (traceObject instanceof MethodTraceObject) {
            MethodTraceObject methodTrace = (MethodTraceObject) traceObject;
            Method method = methodTrace.getMethod();
            if (method != null) {
                node.setMethodSignature(method.getName());
                node.setInterfaceName(method.getDeclaringClass().getSimpleName());
                node.setDubboCall(methodTrace.isDubboCall());
            }
        }

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
