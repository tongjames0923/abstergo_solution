package com.tbs.trace.dubbo;

import com.tbs.trace.core.ITraceIdObjectFounder;
import com.tbs.trace.core.TraceObject;
import org.apache.dubbo.rpc.RpcContext;

import java.util.Optional;

/**
 * 基于RpcContext的TraceId对象查找者
 * <p>
 * 从Dubbo RpcContext的Attachment中查找traceId，并构建TraceObject
 * </p>
 *
 * @author abstergo
 */
public class RpcContextTraceIdObjectFounder implements ITraceIdObjectFounder {

    @Override
    public Optional<TraceObject> findTraceObject() {
        // 从RpcContext获取Attachment中的traceId信息
        String rootTraceId = RpcContext.getServiceContext().getAttachment(DubboTraceContextHolder.ROOT_TRACE_ID_KEY);
        String currentTraceId = RpcContext.getServiceContext().getAttachment(DubboTraceContextHolder.CURRENT_TRACE_ID_KEY);

        // 如果没有传递traceId信息，则返回空
        if (rootTraceId == null || currentTraceId == null) {
            return Optional.empty();
        }

        // 构建TraceObject
        TraceObject traceObject = new TraceObject(rootTraceId);
        traceObject.setContextId(String.valueOf(Thread.currentThread().getId()));

        // 如果有父级traceId，设置父级关系
        if (!rootTraceId.equals(currentTraceId)) {
            traceObject.setParentTraceId(currentTraceId);
        }

        return Optional.of(traceObject);
    }
}