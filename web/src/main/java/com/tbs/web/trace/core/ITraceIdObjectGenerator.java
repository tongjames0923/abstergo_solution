package com.tbs.web.trace.core;

import javax.servlet.http.HttpServletRequest;

/**
 * trceObject的生成器
 *
 * @author abstergo
 */
public interface ITraceIdObjectGenerator {
    /**
     * 生成traceObject
     *
     * @param request
     * @return
     */
    TraceObject generateTraceObject();

}
