package com.tbs.web.trace.impl;

import com.tbs.common.util.UuidUtil;
import com.tbs.web.trace.core.ITraceIdObjectGenerator;
import com.tbs.web.trace.core.TraceObject;

public class UuidTraceObjectGenerator implements ITraceIdObjectGenerator {

    @Override
    public TraceObject generateTraceObject() {
        return new TraceObject(UuidUtil.getInstance().getUuidString());
    }
}
