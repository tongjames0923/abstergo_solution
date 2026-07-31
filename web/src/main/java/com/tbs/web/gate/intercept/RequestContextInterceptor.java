package com.tbs.web.gate.intercept;

import com.tbs.common.data.ValueWrapper;
import com.tbs.web.BaseController;
import com.tbs.web.context.RequestDataContext;
import com.tbs.web.context.RequestContextHolder;
import com.tbs.web.uitl.NetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 请求上下文拦截器
 * <p>
 * 集成到 {@link com.tbs.web.aop.ApiInvokeAop} 的拦截器链中，
 * 负责在Controller方法调用前后管理 {@link RequestDataContext} 的生命周期。
 * <p>
 * 职责：
 * <ul>
 *   <li><b>beforeInvoke</b>：创建或恢复请求上下文，填充基础运行数据（IP、URI、方法名等）</li>
 *   <li><b>afterInvoke</b>：记录请求耗时日志</li>
 *   <li><b>onException</b>：记录异常信息到上下文，便于后续处理</li>
 * </ul>
 * <p>
 * 上下文的清理工作由以下机制保证：
 * <ul>
 *   <li>对于HTTP请求：由 {@link com.tbs.web.gate.I18nFilter} 等Filter在请求结束时统一清理</li>
 *   <li>对于Dubbo调用：由 {@link com.tbs.web.context.propagator.DubboRequestContextFilter} 在调用结束后清理</li>
 * </ul>
 * <p>
 * 典型调用链路：
 * <pre>
 * HTTP请求进入
 *   → I18nFilter（Filter层，可以创建初始上下文）
 *     → ApiInvokeAop → RequestContextInterceptor.beforeInvoke（填充详细数据）
 *       → 业务逻辑（可通过 RequestContextHolder.current() 获取上下文）
 *     → ApiInvokeAop → RequestContextInterceptor.afterInvoke（记录日志）
 *   → I18nFilter finally（清理上下文）
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
     * HTTP请求对象，用于获取请求相关信息
     */
    @Resource
    private HttpServletRequest request;

    /**
     * 方法调用前拦截处理
     * <p>
     * 创建或获取已有的请求上下文，并填充以下基础运行数据：
     * <ul>
     *   <li>clientIp：客户端IP地址</li>
     *   <li>requestMethod：HTTP请求方法</li>
     *   <li>requestUri：请求URI路径</li>
     *   <li>controllerName：控制器类名</li>
     *   <li>methodName：被调用的方法名</li>
     * </ul>
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
            // 获取或创建请求上下文
            RequestDataContext ctx = RequestContextHolder.get();
            if (ctx == null) {
                ctx = RequestDataContext.create();
                RequestContextHolder.set(ctx);
            }

            // 填充基础运行数据
            ctx.setClientIp(NetUtil.getIp(request));
            ctx.setRequestMethod(request.getMethod());
            ctx.setRequestUri(request.getRequestURI());
            ctx.setControllerName(context.getTarget().getClass().getSimpleName());
            ctx.setMethodName(context.getMethod().getName());

            log.debug("请求上下文初始化: {}", ctx);
        } catch (Exception e) {
            log.warn("请求上下文初始化失败", e);
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
