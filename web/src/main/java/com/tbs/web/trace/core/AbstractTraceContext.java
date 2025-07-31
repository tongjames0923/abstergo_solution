package com.tbs.web.trace.core;

import com.tbs.common.holder.context.AbstractNamedContext;
import com.tbs.common.holder.context.IContext;
import com.tbs.common.util.CollUtil;
import com.tbs.common.util.framework.UuidUtil;
import com.tbs.web.trace.tool.ITraceContextListenner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
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

    /**
     * 获取方法调用树提供者
     *
     * @return
     */
    protected abstract IMethodInvokeTreeProvider getMethodInvokeTreeProvider();

    private TraceObject getRootObject() {
        return getMethodInvokeTreeProvider().getTree().getRoot() == null ? null
            : getMethodInvokeTreeProvider().getTree().getRoot().getValue();
    }

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
        return super.isValid() && getRootObject() != null && getRootObject().isValid();
    }

    @Override
    protected void invalidate() {
        super.invalidate();
        if (getRootObject() != null) {
            getRootObject().setValid(false);
            getRootObject().setDisposeTime(new Date());
        }
    }

    @Override
    protected void validate() {
        super.validate();
        if (getRootObject() != null) {
            getRootObject().setValid(true);
            getRootObject().setAvaliableTime(new Date());
        }
    }

    /**
     * 设置根对象
     *
     * @param rootObject
     */
    private void setRootObject(TraceObject rootObject) {
        getMethodInvokeTreeProvider().setMethodRoot(rootObject);
    }

    @Override
    public IContext<TraceObject> rebuildContext() {
        if (isValid()) {
            log.debug("链路跟踪对象尚未失效时被重建");
            if (CollUtil.isNotEmpty(getContextListenners())) {
                for (ITraceContextListenner iTraceContextListenner : getContextListenners()) {
                    iTraceContextListenner.onTraceContextChanged(
                        ITraceContextListenner.CONTEXT_CHANGE_TYPE_CONTEXT_DROPED, this, null, null);
                }
            }
        }
        Collection<ITraceIdObjectFounder> traceIdObjectFounders = getTraceIdObjectFounders();
        if (CollUtil.isNotEmpty(traceIdObjectFounders)) {
            for (ITraceIdObjectFounder traceIdObjectFounder : traceIdObjectFounders) {
                Optional<TraceObject> traceObject = traceIdObjectFounder.findTraceObject();
                if (traceObject.isPresent()) {
                    setRootObject(traceObject.get());
                    break;
                }
            }
        }
        if (getRootObject() == null) {
            setRootObject(getTraceIdObjectGenerator().generateTraceObject());
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
        handleRequestStart(getRootTrace(), request, response);
        if (CollUtil.isNotEmpty(getContextListenners())) {
            for (ITraceContextListenner iTraceContextListenner : getContextListenners()) {
                iTraceContextListenner.onTraceContextChanged(ITraceContextListenner.CONTEXT_CHANGE_TYPE_CONTEXT_CREATED,
                    this, request, response);
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
        handleRequestEnd(getRootTrace(), request, response);
        markInvalid();
        if (CollUtil.isNotEmpty(getContextListenners())) {
            for (ITraceContextListenner iTraceContextListenner : getContextListenners()) {
                iTraceContextListenner.onTraceContextChanged(
                    ITraceContextListenner.CONTEXT_CHANGE_TYPE_CONTEXT_DESTROYED, this, request, response);
            }
        }
    }

    /**
     * 获取TraceObject
     *
     * @return
     */
    public TraceObject getRootTrace() {
        return getRootObject();
    }

    /**
     * 获取当前方法调用的TraceObject
     *
     * @return
     */
    public TraceObject getCurrentMethodTraceObject() {
        return getMethodInvokeTreeProvider().getCurrentTraceObject();
    }

    /**
     * 方法调用开始
     *
     * @param traceId
     * @param method
     * @param args
     * @param target
     */
    public void beginMethod(String traceId, Method method, Object[] args, Object target) {
        MethodTraceObject traceObject =
            new MethodTraceObject(UuidUtil.getInstance().getUuidString(), method, args, target);
        getMethodInvokeTreeProvider().beginMethodFrom(traceId, traceObject);
        if (CollUtil.isNotEmpty(getContextListenners())) {
            for (ITraceContextListenner listenner : getContextListenners()) {
                listenner.onMethodInvokeBegin(traceObject, getMethodInvokeTreeProvider());
            }
        }
    }

    /**
     * 方法调用结束
     *
     * @param traceId
     */
    public void endMethod(String traceId) {
        TraceObject traceObject = getMethodInvokeTreeProvider().endMethod(traceId);
        if (CollUtil.isNotEmpty(getContextListenners())) {
            for (ITraceContextListenner listenner : getContextListenners()) {
                listenner.onMethodInvokeEnd(traceObject, getMethodInvokeTreeProvider());
            }
        }
    }
}
