package com.tbs.web.trace.tool;

import com.tbs.web.trace.core.AbstractTraceContext;
import com.tbs.web.trace.core.TraceObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 链路跟踪环境监听器
 *
 * @author abstergo
 */
public interface ITraceContextListenner {
    /**
     * 链路跟踪环境可用
     *
     * @param trace
     * @param request
     * @param response
     * @param traceContext
     */
    void onTraceAvaliable(TraceObject trace, HttpServletRequest request, HttpServletResponse response,
        AbstractTraceContext traceContext);

    /**
     * 链路跟踪环境不可用
     *
     * @param trace
     * @param request
     * @param response
     * @param traceContext
     */
    void onTraceUnavaliable(TraceObject trace, HttpServletRequest request, HttpServletResponse response,
        AbstractTraceContext traceContext);
}
