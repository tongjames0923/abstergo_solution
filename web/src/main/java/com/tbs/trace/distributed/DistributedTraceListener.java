package com.tbs.trace.distributed;

import com.alibaba.fastjson2.JSON;
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
import java.lang.reflect.Parameter;
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

    private static final int MAX_ARGS_DESCRIPTION_LENGTH = 500;

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

        if (!traceProperty.isDistributedTraceEnabled()) {
            return;
        }

        TraceObject rootTrace = traceContext.getRootTrace();
        if (rootTrace == null) {
            return;
        }

        try {
            TraceNode rootNode = extractTraceTree(traceContext, request);
            if (rootNode == null) {
                return;
            }

            // 根节点注入HTTP信息
            if (request != null) {
                rootNode.setHttpMethod(request.getMethod());
                rootNode.setHttpUrl(request.getRequestURI());
                rootNode.setRemoteAddr(getClientIp(request));
                rootNode.setUserAgent(request.getHeader("User-Agent"));
                rootNode.setContentType(request.getContentType());
                rootNode.setQueryString(request.getQueryString());
                rootNode.setThreadName(Thread.currentThread().getName());
                rootNode.setThreadId(Thread.currentThread().getId());
            }
            rootNode.setServiceName(serviceName);
            if (response != null) {
                rootNode.setHttpStatus(response.getStatus());
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

    private TraceNode extractTraceTree(AbstractTraceContext traceContext, HttpServletRequest request) {
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
        node.setContextId(traceObject.getContextId());

        if (traceObject instanceof MethodTraceObject) {
            MethodTraceObject methodTrace = (MethodTraceObject) traceObject;
            Method method = methodTrace.getMethod();
            if (method != null) {
                node.setMethodSignature(method.getName());
                node.setInterfaceName(method.getDeclaringClass().getSimpleName());

                // 实现类名（target的真实类型）
                Object target = methodTrace.getTarget();
                if (target != null) {
                    String implClass = target.getClass().getSimpleName();
                    // 如果实现类和接口不同，记录实现类
                    if (!implClass.equals(method.getDeclaringClass().getSimpleName())) {
                        node.setClassName(implClass);
                    }
                }

                // 参数类型
                Parameter[] parameters = method.getParameters();
                if (parameters != null && parameters.length > 0) {
                    StringBuilder paramTypesSb = new StringBuilder();
                    for (int i = 0; i < parameters.length; i++) {
                        if (i > 0) {
                            paramTypesSb.append(", ");
                        }
                        paramTypesSb.append(parameters[i].getType().getSimpleName())
                            .append(" ").append(parameters[i].getName());
                    }
                    node.setParamTypes(paramTypesSb.toString());
                }

                // 参数值摘要（安全序列化，截断过长内容）
                Object[] args = methodTrace.getArgs();
                if (args != null && args.length > 0) {
                    try {
                        String argsJson = JSON.toJSONString(args);
                        if (argsJson.length() > MAX_ARGS_DESCRIPTION_LENGTH) {
                            argsJson = argsJson.substring(0, MAX_ARGS_DESCRIPTION_LENGTH) + "...";
                        }
                        node.setArgsDescription(argsJson);
                    } catch (Exception e) {
                        node.setArgsDescription("[序列化失败]");
                    }
                }

                node.setDubboCall(methodTrace.isDubboCall());
            }
        }

        // 线程信息
        node.setThreadName(Thread.currentThread().getName());
        node.setThreadId(Thread.currentThread().getId());

        // 递归构建子节点
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

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(",")).trim();
        }
        return ip;
    }
}
