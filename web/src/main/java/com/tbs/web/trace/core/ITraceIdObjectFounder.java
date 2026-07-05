package com.tbs.web.trace.core;

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
     * @return 如果找到则返回TraceObject，否则返回空
     */
    Optional<TraceObject> findTraceObject();
}
