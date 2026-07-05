package com.tbs.trace.dubbo;

import com.tbs.common.AbstractAutoBeanRegister;
import com.tbs.common.Feature;
import com.tbs.trace.config.TraceProperty;
import com.tbs.trace.core.AbstractTraceContext;
import com.tbs.trace.core.TraceObject;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

import javax.annotation.Resource;
import java.lang.reflect.Method;

/**
 * Dubbo Provider端Trace过滤器
 * <p>
 * 在Provider端接收远程调用时，从RpcContext获取traceId并创建trace上下文
 * </p>
 *
 * @author abstergo
 */
@Activate(group = CommonConstants.PROVIDER, order = -10000)
public class DubboTraceProviderFilter implements Filter {

    @Resource
    private AbstractTraceContext traceContext;

    @Resource
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

        // 获取trace上下文，如果不存在则创建
        if (traceContext == null) {
            return invoker.invoke(invocation);
        }

        // 重建trace上下文，从RpcContext中获取traceId
        traceContext.rebuild();
        
        // 获取RpcContext中的traceId信息，用于构建分布式调用树
        String rootTraceId = RpcContext.getServiceContext().getAttachment(DubboTraceContextHolder.ROOT_TRACE_ID_KEY);
        String parentTraceId = RpcContext.getServiceContext().getAttachment(DubboTraceContextHolder.CURRENT_TRACE_ID_KEY);
        
        // 如果没有获取到traceId信息，使用本地rootTrace
        if (rootTraceId == null || parentTraceId == null) {
            TraceObject rootTrace = traceContext.getRootTrace();
            if (rootTrace == null || !rootTrace.isValid()) {
                return invoker.invoke(invocation);
            }
            rootTraceId = rootTrace.getTraceId();
            parentTraceId = rootTrace.getTraceId();
        }
        
        // 创建当前方法的trace节点，挂载在Consumer的Dubbo节点下
        try {
            Method method =
                invoker.getInterface().getMethod(invocation.getMethodName(), invocation.getParameterTypes());
            // 使用parentTraceId（Consumer的Dubbo节点traceId）作为父级
            // 这样Provider的本地树结构为：
            //   Root(parentTraceId) -> 方法节点
            // 分布式拼接时，该Root会被插入到Consumer的Dubbo节点下
            traceContext.beginMethod(parentTraceId, method, invocation.getArguments(), invoker.getInterface());
        
            Result result = invoker.invoke(invocation);
        
            return result;
        } catch (RpcException e) {
            throw e;
        } catch (NoSuchMethodException e) {
            throw new RpcException(e);
        } finally {
            String currentTraceId = traceContext.getCurrentMethodTraceObject().getTraceId();
            traceContext.endMethod(currentTraceId);
        }
    }
}
