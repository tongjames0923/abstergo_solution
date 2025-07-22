package com.tbs.web.trace.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.web.trace.core.AbstractTraceContext;
import com.tbs.web.trace.core.AbstractTraceInterceptor;

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
