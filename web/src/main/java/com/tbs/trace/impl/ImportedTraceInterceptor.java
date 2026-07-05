package com.tbs.trace.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.trace.core.AbstractTraceContext;
import com.tbs.trace.core.AbstractTraceInterceptor;

/**
 * 导入的Trace拦截器
 *
 * @author abstergo
 */
public class ImportedTraceInterceptor extends AbstractTraceInterceptor {
    private AbstractTraceContext traceContext;

    @Override
    protected AbstractTraceContext getTraceContext() {
        return traceContext;
    }

    public ImportedTraceInterceptor(AbstractTraceContext traceContext) {
        AssertUtil.notNull(traceContext, "traceContext can not be null");
        this.traceContext = traceContext;
    }
}
