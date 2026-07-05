package com.tbs.trace.dubbo;

import com.tbs.common.AbstractAutoBeanRegister;
import com.tbs.common.Feature;
import com.tbs.trace.config.TraceProperty;
import com.tbs.trace.core.AbstractTraceContext;
import com.tbs.trace.core.TraceObject;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

import java.lang.reflect.Method;

/**
 * Dubbo Provider端Trace过滤器
 * <p>
 * 在Provider端接收远程调用时，从RpcContext获取traceId并创建trace上下文。
 * 使用AbstractTraceContext.activate()/deactivate()管理上下文生命周期，
 * 通过ThreadLocal实现线程隔离，兼容HTTP和Dubbo线程。
 * </p>
 *
 * @author abstergo
 */
@Activate(group = CommonConstants.PROVIDER, order = -10000)
public class DubboTraceProviderFilter implements Filter {

    private AbstractTraceContext traceContext;

    private TraceProperty traceProperty;

    public void setTraceContext(AbstractTraceContext traceContext) {
        this.traceContext = traceContext;
    }

    public void setTraceProperty(TraceProperty traceProperty) {
        this.traceProperty = traceProperty;
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if (!AbstractAutoBeanRegister.isFeatureRegistered(Feature.TRACE)) {
            return invoker.invoke(invocation);
        }

        if (traceProperty != null && !traceProperty.isDubboTraceEnabled()) {
            return invoker.invoke(invocation);
        }

        if (traceContext == null) {
            return invoker.invoke(invocation);
        }

        // 激活trace上下文（ThreadLocal，每个Dubbo线程独立）
        traceContext.activate();

        try {
            // 从RpcContext获取Consumer端传递的traceId
            String parentTraceId = RpcContext.getServiceContext()
                .getAttachment(DubboTraceContextHolder.CURRENT_TRACE_ID_KEY);

            if (parentTraceId == null) {
                // 没有Consumer传递的traceId，使用本地root
                TraceObject rootTrace = traceContext.getRootTrace();
                if (rootTrace == null || !rootTrace.isValid()) {
                    return invoker.invoke(invocation);
                }
                parentTraceId = rootTrace.getTraceId();
            }

            // 创建当前方法的trace节点
            Method method =
                invoker.getInterface().getMethod(invocation.getMethodName(), invocation.getParameterTypes());
            traceContext.beginMethod(parentTraceId, method, invocation.getArguments(), invoker.getInterface());

            Result result = invoker.invoke(invocation);

            return result;
        } catch (RpcException e) {
            throw e;
        } catch (NoSuchMethodException e) {
            throw new RpcException(e);
        } finally {
            try {
                String currentTraceId = traceContext.getCurrentMethodTraceObject().getTraceId();
                traceContext.endMethod(currentTraceId);
            } catch (Exception ignored) {
            }
            // 停用trace上下文，清理ThreadLocal
            traceContext.deactivate();
        }
    }
}
