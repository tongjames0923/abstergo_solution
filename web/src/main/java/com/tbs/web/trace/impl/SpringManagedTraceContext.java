package com.tbs.web.trace.impl;

import com.tbs.common.holder.context.IContext;
import com.tbs.common.util.SpringUtil;
import com.tbs.web.trace.core.*;
import com.tbs.web.trace.tool.ITraceContextListenner;
import org.slf4j.Logger;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Spring管理TraceContext
 *
 * @author abstergo
 */
public class SpringManagedTraceContext extends AbstractTraceContext {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(SpringManagedTraceContext.class);

    @Resource
    private ITraceIdObjectGenerator traceIdObjectGenerator;

    @Resource
    private IMethodInvokeTreeProvider methodInvokeTreeProvider;

    @Override
    protected IContext<TraceObject> rebuild() {
        return this;
    }

    @Override
    protected IMethodInvokeTreeProvider getMethodInvokeTreeProvider() {
        return methodInvokeTreeProvider;
    }

    @Override
    protected Collection<ITraceContextListenner> getContextListenners() {
        try {
            Map<String, ITraceContextListenner> traceIdObjectFounderMap =
                SpringUtil.getBeansOfType(ITraceContextListenner.class);
            return traceIdObjectFounderMap.values();
        } catch (Exception e) {
            log.debug("getTraceIdObjectFounders error", e);
            return Collections.emptyList();
        }
    }

    @Override
    protected Collection<ITraceIdObjectFounder> getTraceIdObjectFounders() {
        try {
            Map<String, ITraceIdObjectFounder> traceIdObjectFounderMap =
                SpringUtil.getBeansOfType(ITraceIdObjectFounder.class);
            return traceIdObjectFounderMap.values();
        } catch (Exception e) {
            log.debug("getTraceIdObjectFounders error", e);
            return Collections.emptyList();
        }
    }

    @Override
    protected ITraceIdObjectGenerator getTraceIdObjectGenerator() {
        return traceIdObjectGenerator;
    }

    @Override
    protected void handleRequestStart(TraceObject traceObject, HttpServletRequest request,
        HttpServletResponse response) {

    }

    @Override
    protected void handleRequestEnd(TraceObject traceObject, HttpServletRequest request, HttpServletResponse response) {

    }
}
