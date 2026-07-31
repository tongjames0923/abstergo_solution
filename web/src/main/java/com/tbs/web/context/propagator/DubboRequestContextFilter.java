package com.tbs.web.context.propagator;

import com.alibaba.fastjson2.JSON;
import com.tbs.web.context.RequestDataContext;
import com.tbs.web.context.RequestContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Dubbo请求上下文传播过滤器
 * <p>
 * 通过Dubbo Filter机制，在分布式RPC调用前后自动传播 {@link RequestDataContext}。
 * <p>
 * 工作原理：
 * <ul>
 *   <li><b>Consumer端（调用方）</b>：在RPC调用前，将当前线程的RequestDataContext序列化后
 *       放入RpcContext attachments，随RPC请求一起发送到下游服务</li>
 *   <li><b>Provider端（被调方）</b>：在RPC调用后，从RpcContext attachments中提取
 *       RequestDataContext，反序列化并设置到当前线程的RequestContextHolder中</li>
 * </ul>
 * <p>
 * 这样，下游服务在处理业务逻辑时，可以通过 {@link RequestContextHolder#current()}
 * 获取到与上游服务相同的请求上下文数据，实现分布式场景下的上下文透传。
 * <p>
 * 传播的数据包括：
 * <ul>
 *   <li>traceId：链路追踪ID，用于关联分布式调用链</li>
 *   <li>startTime：请求开始时间</li>
 *   <li>clientIp：客户端IP</li>
 *   <li>businessData：业务数据Map</li>
 * </ul>
 *
 * @author abstergo
 * @see RequestDataContext
 * @see RequestContextHolder
 */
@Slf4j
@Activate(group = {CommonConstants.CONSUMER, CommonConstants.PROVIDER}, order = -10000)
public class DubboRequestContextFilter implements Filter {

    /**
     * RpcContext attachments中存放请求上下文的key
     */
    static final String CONTEXT_KEY = "tbs.request.context";

    /**
     * RpcContext attachments中存放traceId的key（轻量级，仅传递traceId）
     */
    static final String TRACE_ID_KEY = "tbs.request.traceId";

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        RpcContext context = RpcContext.getContext();

        if (context.isConsumerSide()) {
            // ============ Consumer端：将上下文注入到RpcContext ============
            return consumerInvoke(invoker, invocation, context);
        } else {
            // ============ Provider端：从RpcContext恢复上下文 ============
            return providerInvoke(invoker, invocation, context);
        }
    }

    /**
     * Consumer端处理：将当前线程的RequestDataContext传播到RPC调用中
     */
    private Result consumerInvoke(Invoker<?> invoker, Invocation invocation, RpcContext rpcContext) {
        RequestDataContext ctx = RequestContextHolder.get();
        if (ctx != null) {
            try {
                // 序列化上下文并放入attachments
                String serialized = Base64.getEncoder().encodeToString(
                    JSON.toJSONString(ctx).getBytes(StandardCharsets.UTF_8));
                rpcContext.setAttachment(CONTEXT_KEY, serialized);
                // 轻量级traceId也单独放一份，方便下游快速获取
                rpcContext.setAttachment(TRACE_ID_KEY, ctx.getTraceId());
            } catch (Exception e) {
                log.warn("Dubbo请求上下文传播失败(Consumer端)", e);
            }
        }

        try {
            return invoker.invoke(invocation);
        } finally {
            // Consumer端调用结束后，清理attachments中的上下文（避免污染后续调用）
            rpcContext.removeAttachment(CONTEXT_KEY);
            rpcContext.removeAttachment(TRACE_ID_KEY);
        }
    }

    /**
     * Provider端处理：从RPC调用中恢复RequestDataContext到当前线程
     */
    private Result providerInvoke(Invoker<?> invoker, Invocation invocation, RpcContext rpcContext) {
        RequestDataContext previousCtx = RequestContextHolder.get();
        boolean needRestore = false;

        try {
            // 从attachments中提取上下文
            String serialized = rpcContext.getAttachment(CONTEXT_KEY);
            if (serialized != null && !serialized.isEmpty()) {
                try {
                    byte[] bytes = Base64.getDecoder().decode(serialized);
                    RequestDataContext ctx = JSON.parseObject(
                        new String(bytes, StandardCharsets.UTF_8), RequestDataContext.class);
                    if (ctx != null) {
                        RequestContextHolder.set(ctx);
                        log.debug("Provider端恢复请求上下文: traceId={}", ctx.getTraceId());
                    }
                } catch (Exception e) {
                    log.warn("Provider端请求上下文反序列化失败", e);
                }
            } else {
                // 没有完整上下文，但可能有traceId
                String traceId = rpcContext.getAttachment(TRACE_ID_KEY);
                if (traceId != null && !traceId.isEmpty()) {
                    RequestDataContext ctx = RequestDataContext.create(traceId);
                    RequestContextHolder.set(ctx);
                }
            }

            return invoker.invoke(invocation);
        } finally {
            // Provider端调用结束后，清理上下文并恢复之前的上下文
            RequestContextHolder.set(previousCtx);
            needRestore = true;
        }
    }

    /**
     * 获取RpcContext中传播的traceId（用于日志等场景）
     *
     * @return traceId，可能为null
     */
    public static String getTraceIdFromRpcContext() {
        return RpcContext.getContext().getAttachment(TRACE_ID_KEY);
    }
}
