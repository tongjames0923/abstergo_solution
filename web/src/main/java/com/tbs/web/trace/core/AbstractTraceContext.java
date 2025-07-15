package com.tbs.web.trace.core;

import com.tbs.common.holder.context.AbstractNamedContext;
import com.tbs.common.holder.context.IContext;
import com.tbs.common.util.CollUtil;
import com.tbs.web.trace.tool.ITraceContextListenner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;

/**
 * 抽象的TraceContext
 *
 * @author abstergo
 */
public abstract class AbstractTraceContext extends AbstractNamedContext<TraceObject> {

    public static final String CONTEXT_ID = "trace-context-id";

    private static final Logger log = LoggerFactory.getLogger(AbstractTraceContext.class);

    TraceObject traceObject = null;

    /**
     * 获取当前TraceObject
     *
     * @return
     */
    protected abstract Collection<ITraceContextListenner> getContextListenners();

    /**
     * 构造函数
     *
     * @param contextId 上下文ID
     */
    public AbstractTraceContext() {
        super(CONTEXT_ID);
    }

    /**
     * 获取TraceIdObjectFounder
     *
     * @return
     */
    protected abstract Collection<ITraceIdObjectFounder> getTraceIdObjectFounders();

    /**
     * 获取TraceIdObjectGenerator
     *
     * @return
     */
    protected abstract ITraceIdObjectGenerator getTraceIdObjectGenerator();

    @Override
    public boolean isValid() {
        return super.isValid() && traceObject != null && traceObject.isValid();
    }

    @Override
    protected void invalidate() {
        super.invalidate();
        if (traceObject != null) {
            traceObject.setValid(false);
            traceObject.setDisposeTime(new Date());
        }
    }

    @Override
    protected void validate() {
        super.validate();
        if (traceObject != null) {
            traceObject.setValid(true);
            traceObject.setAvaliableTime(new Date());
        }
    }

    @Override
    public IContext<TraceObject> rebuildContext() {
        if (isValid()) {
            log.warn("链路跟踪对象尚未失效时被重建");
        }
        Collection<ITraceIdObjectFounder> traceIdObjectFounders = getTraceIdObjectFounders();
        if (CollUtil.isNotEmpty(traceIdObjectFounders)) {
            for (ITraceIdObjectFounder traceIdObjectFounder : traceIdObjectFounders) {
                Optional<TraceObject> traceObject = traceIdObjectFounder.findTraceObject();
                if (traceObject.isPresent()) {
                    this.traceObject = traceObject.get();
                    break;
                }
            }
        }
        if (traceObject == null) {
            traceObject = getTraceIdObjectGenerator().generateTraceObject();
        }
        return super.rebuildContext();
    }

    /**
     * 请求开始处理
     *
     * @param traceObject
     * @param request
     * @param response
     */
    protected abstract void handleRequestStart(TraceObject traceObject, HttpServletRequest request,
        HttpServletResponse response);

    /**
     * 请求结束处理
     *
     * @param traceObject
     * @param request
     * @param response
     */
    protected abstract void handleRequestEnd(TraceObject traceObject, HttpServletRequest request,
        HttpServletResponse response);

    /**
     * 创建上下文可用，会优先直接调用rebuildContext
     *
     * @param request
     * @param response
     */
    public void makeContextAvaliable(HttpServletRequest request, HttpServletResponse response) {
        rebuildContext();
        handleRequestStart(getTrace(), request, response);
        if (CollUtil.isNotEmpty(getContextListenners())) {
            for (ITraceContextListenner iTraceContextListenner : getContextListenners()) {
                iTraceContextListenner.onTraceAvaliable(getTrace(), request, response, this);
            }
        }
    }

    /**
     * 使得上下文不可用，会最后调用直接调用markInvalid
     *
     * @param request
     * @param response
     */
    public void makeContextUnavaliable(HttpServletRequest request, HttpServletResponse response) {
        handleRequestEnd(getTrace(), request, response);
        markInvalid();
        if (CollUtil.isNotEmpty(getContextListenners())) {
            for (ITraceContextListenner iTraceContextListenner : getContextListenners()) {
                iTraceContextListenner.onTraceUnavaliable(getTrace(), request, response, this);
            }
        }
    }

    /**
     * 获取TraceObject
     *
     * @return
     */
    public TraceObject getTrace() {
        return traceObject;
    }

}
