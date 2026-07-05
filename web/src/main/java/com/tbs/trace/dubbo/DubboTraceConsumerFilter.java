package com.tbs.trace.dubbo;

import com.tbs.common.Feature;
import com.tbs.common.AbstractAutoBeanRegister;
import com.tbs.trace.config.TraceProperty;
import com.tbs.trace.core.AbstractTraceContext;
import com.tbs.trace.core.TraceObject;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

import javax.annotation.Resource;
import java.lang.reflect.Method;

/**
 * Dubbo Consumer端Trace过滤器
 * <p>
 * 在Consumer端调用远程服务时，将当前traceId放入RpcContext Attachment
 * </p>
 *
 * @author abstergo
 */
@Activate(group = CommonConstants.CONSUMER, order = -10000)
public class DubboTraceConsumerFilter implements Filter {

    @Resource
    private AbstractTraceContext traceContext;

    private TraceProperty traceProperty;

    /**
     * 设置trace上下文
     *
     * @param traceContext trace上下文
     */
    public void setTraceContext(AbstractTraceContext traceContext) {
        this.traceContext = traceContext;
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 检查Trace功能是否启用
        if (!AbstractAutoBeanRegister.isFeatureRegistered(Feature.TRACE)) {
            return invoker.invoke(invocation);
        }

        // 检查Dubbo Trace是否启用
        if (traceProperty != null && !traceProperty.isDubboTraceEnabled()) {
            return invoker.invoke(invocation);
        }

        // 获取当前trace上下文
        if (traceContext == null || !traceContext.isValid()) {
            return invoker.invoke(invocation);
        }

        TraceObject rootTrace = traceContext.getRootTrace();
        TraceObject currentTrace = traceContext.getCurrentMethodTraceObject();

        if (rootTrace == null || currentTrace == null) {
            return invoker.invoke(invocation);
        }

        // 在RpcContext中设置traceId信息
        RpcContext.getClientAttachment().setAttachment(
            DubboTraceContextHolder.ROOT_TRACE_ID_KEY, rootTrace.getTraceId());
        RpcContext.getClientAttachment().setAttachment(
            DubboTraceContextHolder.CURRENT_TRACE_ID_KEY, currentTrace.getTraceId());

        try {
            // 在trace树中创建Dubbo调用节点
            Method method = invoker.getInterface().getMethod(
                invocation.getMethodName(), invocation.getParameterTypes());
            traceContext.beginMethod(
                currentTrace.getTraceId(), method, invocation.getArguments(), invoker.getInterface(), true);

            Result result = invoker.invoke(invocation);

            // 调用结束后关闭节点
            String dubboTraceId = traceContext.getCurrentMethodTraceObject().getTraceId();
            traceContext.endMethod(dubboTraceId);

            return result;
        } catch (RpcException e) {
            // 异常时也要关闭节点
            String dubboTraceId = traceContext.getCurrentMethodTraceObject().getTraceId();
            traceContext.endMethod(dubboTraceId);
            throw e;
        } catch (NoSuchMethodException e) {
            throw new RpcException(e);
        }
    }
}