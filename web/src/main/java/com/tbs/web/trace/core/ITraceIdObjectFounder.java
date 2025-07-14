package com.tbs.web.trace.core;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * TraceObject查找者
 *
 * @author abstergo
 */
public interface ITraceIdObjectFounder {
    /**
     * 查找现有的TraceObject
     *
     * @param request
     * @return
     */
    Optional<TraceObject> findTraceObject(HttpServletRequest request);
}
