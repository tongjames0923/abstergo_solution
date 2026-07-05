package com.tbs.trace.core;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 链路跟踪的拦截器
 * @author abstergo
 */
public abstract class AbstractTraceInterceptor implements HandlerInterceptor {

    /**
     * 获取Trace上下文
     *
     * @return
     */
    protected abstract AbstractTraceContext getTraceContext();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        getTraceContext().makeContextAvailable(request, response);
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
        throws Exception {
        getTraceContext().makeContextUnavailable(request, response);
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
