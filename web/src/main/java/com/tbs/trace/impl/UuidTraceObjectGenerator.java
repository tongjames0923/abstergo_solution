package com.tbs.trace.impl;

import com.tbs.common.util.framework.UuidUtil;
import com.tbs.trace.core.ITraceIdObjectGenerator;
import com.tbs.trace.core.TraceObject;

/**
 * UUID生成的TraceObject
 * @author abstergo
 */
public class UuidTraceObjectGenerator implements ITraceIdObjectGenerator {
    @Override
    public TraceObject generateTraceObject() {
        return new TraceObject(UuidUtil.getInstance().getUuidString());
    }
}
