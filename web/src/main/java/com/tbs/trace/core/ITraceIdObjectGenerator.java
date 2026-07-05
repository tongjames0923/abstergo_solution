package com.tbs.trace.core;

/**
 * traceObject的生成器
 *
 * @author abstergo
 */
public interface ITraceIdObjectGenerator {
    /**
     * 生成traceObject
     *
     * @return 新生成的TraceObject实例
     */
    TraceObject generateTraceObject();

}
