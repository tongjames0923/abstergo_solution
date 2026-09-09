package com.tbs.web.gate.intercept;

import com.tbs.common.data.ValueWrapper;
import com.tbs.web.context.RequestDataContext;
import com.tbs.web.context.RequestContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 请求上下文拦截器
 * <p>
 * 集成到 {@link com.tbs.web.aop.ApiInvokeAop} 的拦截器链中，
 * 在 Controller 方法调用前后补充管理 {@link RequestDataContext}。
 * <p>
 * 职责：
 * <ul>
 *   <li><b>beforeInvoke</b>：补充填充 Controller 层信息（controllerName、methodName），
 *       该信息仅切点可知，Filter 层无法获取。上下文由 {@code RequestContextLifecycleFilter}
 *       的 {@code IContextBuilder} 生成管道在最外层创建，本类不重建——
 *       中途新建会导致 traceId 断链；上下文缺失（生成管道异常）时仅记 warn 跳过</li>
 *   <li><b>afterInvoke</b>：记录请求耗时日志（Controller 级唯一耗时观测点）</li>
 *   <li><b>onException</b>：记录异常信息到上下文，便于后续处理</li>
 * </ul>
 * <p>
 * 上下文的清理工作由以下机制保证：
 * <ul>
 *   <li>对于HTTP请求：由 {@code RequestContextLifecycleFilter} 在 finally 中经
 *       {@code IContextClean} 清理管道统一清理（含 {@code RequestContextClean}、
 *       {@code I18nContextClean} 等实现）</li>
 *   <li>对于Dubbo调用：由 {@link com.tbs.web.context.propagator.DubboRequestContextFilter} 在调用结束后清理</li>
 * </ul>
 * <p>
 * 典型调用链路：
 * <pre>
 * HTTP请求进入
 *   → RequestContextLifecycleFilter（Filter层，IContextBuilder 管道创建/填充上下文）
 *     → ApiInvokeAop → RequestContextInterceptor.beforeInvoke（补充 Controller 层信息）
 *       → 业务逻辑（可通过 RequestContextHolder.current() 获取上下文）
 *     → ApiInvokeAop → RequestContextInterceptor.afterInvoke（记录日志）
 *   → RequestContextLifecycleFilter finally（IContextClean 管道统一清理上下文）
 * </pre>
 *
 * @author abstergo
 * @see RequestDataContext
 * @see RequestContextHolder
 * @see com.tbs.web.aop.ApiInvokeAop
 */
@Slf4j
@Component
public class RequestContextInterceptor implements IApiInvokeInterceptor {

    /**
     * 方法调用前拦截处理
     * <p>
     * 补充填充 Controller 层信息（controllerName、methodName）到已有的请求上下文。
     * 上下文由 {@code RequestContextLifecycleFilter} 在过滤器链最外层创建并填充
     * 基础运行数据（clientIp、requestMethod、requestUri），本类不重复填充、不重建。
     * 上下文缺失（生成管道异常降级）时仅记 warn 跳过，避免中途新建导致 traceId 断链。
     * </p>
     *
     * @param context API调用上下文，包含目标对象、方法和参数等信息
     * @return 原样返回的API调用上下文
     */
    @Override
    public ApiInvokeContext beforeInvoke(ApiInvokeContext context) {
        if (context == null) {
            return null;
        }

        try {
            RequestDataContext ctx = RequestContextHolder.get();
            if (ctx == null) {
                log.warn("请求上下文缺失，跳过 Controller 层信息填充: {}.{}",
                    context.getTarget().getClass().getSimpleName(), context.getMethod().getName());
                return context;
            }
            ctx.setControllerName(context.getTarget().getClass().getSimpleName());
            ctx.setMethodName(context.getMethod().getName());
        } catch (Exception e) {
            log.warn("请求上下文 Controller 层信息填充失败", e);
        }

        return context;
    }

    /**
     * 方法调用后拦截处理
     * <p>
     * 记录请求处理耗时日志，上下文数据保持不变供后续使用
     *
     * @param context API调用上下文
     * @param result  方法执行结果
     */
    @Override
    public void afterInvoke(ApiInvokeContext context, ValueWrapper<Object> result) {
        if (context == null) {
            return;
        }

        try {
            RequestDataContext ctx = RequestContextHolder.get();
            if (ctx != null) {
                log.debug("请求处理完成: traceId={}, 耗时={}ms, 方法={}.{}",
                    ctx.getTraceId(), ctx.elapsed(),
                    ctx.getControllerName(), ctx.getMethodName());
            }
        } catch (Exception e) {
            log.warn("请求上下文日志记录失败", e);
        }
    }

    /**
     * 异常处理拦截
     * <p>
     * 将异常信息记录到请求上下文中，便于分布式链路追踪和异常分析
     *
     * @param context API调用上下文
     * @param e       方法执行过程中抛出的异常
     */
    @Override
    public void onException(ApiInvokeContext context, Throwable e) {
        if (context == null) {
            return;
        }

        try {
            RequestDataContext ctx = RequestContextHolder.get();
            if (ctx != null) {
                ctx.put("_exception", e.getClass().getSimpleName());
                ctx.put("_errorMessage", e.getMessage());
                log.warn("请求异常: traceId={}, 耗时={}ms, 异常={}, 方法={}.{}",
                    ctx.getTraceId(), ctx.elapsed(),
                    e.getClass().getSimpleName(),
                    ctx.getControllerName(), ctx.getMethodName());
            }
        } catch (Exception ex) {
            log.warn("请求上下文异常记录失败", ex);
        }
    }
}
