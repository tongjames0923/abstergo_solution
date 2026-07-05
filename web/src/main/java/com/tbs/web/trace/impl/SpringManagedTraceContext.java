package com.tbs.web.trace.impl;

import com.tbs.common.util.framework.SpringUtil;
import com.tbs.web.trace.core.*;
import com.tbs.web.trace.tool.ITraceContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger log = LoggerFactory.getLogger(SpringManagedTraceContext.class);

    @Resource
    private ITraceIdObjectGenerator traceIdObjectGenerator;

    @Resource
    private IMethodInvokeTreeProvider methodInvokeTreeProvider;


    @Override
    protected IMethodInvokeTreeProvider getMethodInvokeTreeProvider() {
        return methodInvokeTreeProvider;
    }

    @Override
    protected Collection<ITraceContextListener> getContextListeners() {
        try {
            Map<String, ITraceContextListener> listenerMap =
                SpringUtil.getBeansOfType(ITraceContextListener.class);
            return listenerMap.values();
        } catch (Exception e) {
            log.debug("getContextListeners error", e);
            return Collections.emptyList();
        }
    }

    @Override
    protected Collection<ITraceIdObjectFounder> getTraceIdObjectFounders() {
        try {
            Map<String, ITraceIdObjectFounder> founderMap =
                SpringUtil.getBeansOfType(ITraceIdObjectFounder.class);
            return founderMap.values();
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
