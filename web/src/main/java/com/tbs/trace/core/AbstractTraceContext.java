package com.tbs.trace.core;

import com.tbs.common.context.IContext;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.framework.UuidUtil;
import com.tbs.trace.tool.ITraceContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * 抽象的TraceContext
 *
 * @author abstergo
 */
public abstract class AbstractTraceContext implements IContext {


    private static final Logger log = LoggerFactory.getLogger(AbstractTraceContext.class);

    /**
     * 获取方法调用树提供者
     *
     * @return 方法调用树提供者实例
     */
    protected abstract IMethodInvokeTreeProvider getMethodInvokeTreeProvider();

    private TraceObject getRootObject() {
        return getMethodInvokeTreeProvider().getTree().getRoot() == null ? null
            : getMethodInvokeTreeProvider().getTree().getRoot().getValue();
    }

    /**
     * 获取当前TraceObject
     *
     * @return 当前监听器集合
     */
    protected abstract Collection<ITraceContextListener> getContextListeners();

    /**
     * 获取TraceIdObjectFounder
     *
     * @return TraceIdObjectFounder集合
     */
    protected abstract Collection<ITraceIdObjectFounder> getTraceIdObjectFounders();

    /**
     * 获取TraceIdObjectGenerator
     *
     * @return TraceIdObjectGenerator实例
     */
    protected abstract ITraceIdObjectGenerator getTraceIdObjectGenerator();

    private boolean validate = false;
    @Override
    public boolean isValid() {
        return validate && getRootObject() != null && getRootObject().isValid();
    }

    protected void invalidate() {
        validate = false;
        if (getRootObject() != null) {
            getRootObject().setValid(false);
            getRootObject().setDisposeTime(new Date());
        }
    }

    protected void validate() {
        validate = true;
        if (getRootObject() != null) {
            getRootObject().setValid(true);
            getRootObject().setAvailableTime(new Date());
        }
    }

    /**
     * 设置根对象
     *
     * @param rootObject 根TraceObject
     */
    private void setRootObject(TraceObject rootObject) {
        getMethodInvokeTreeProvider().setMethodRoot(rootObject);
    }

    @Override
    public void destroy() {
        invalidate();
    }

    @Override
    public void rebuild() {
        if (isValid()) {
            log.debug("链路跟踪对象尚未失效时被重建");
            notifyListeners((listener, ctx) ->
                listener.onTraceContextChanged(ITraceContextListener.CONTEXT_CHANGE_TYPE_CONTEXT_DROPPED, ctx, null, null));
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
        validate();
    }

    /**
     * 请求开始处理
     *
     * @param traceObject 根TraceObject
     * @param request
     * @param response
     */
    protected abstract void handleRequestStart(TraceObject traceObject, HttpServletRequest request,
        HttpServletResponse response);

    /**
     * 请求结束处理
     *
     * @param traceObject 根TraceObject
     * @param request
     * @param response
     */
    protected abstract void handleRequestEnd(TraceObject traceObject, HttpServletRequest request,
        HttpServletResponse response);

    /**
     * 创建上下文可用，会优先直接调用rebuild
     *
     * @param request
     * @param response
     */
    public void makeContextAvailable(HttpServletRequest request, HttpServletResponse response) {
        rebuild();
        handleRequestStart(getRootTrace(), request, response);
        notifyListeners((listener, ctx) ->
            listener.onTraceContextChanged(ITraceContextListener.CONTEXT_CHANGE_TYPE_CONTEXT_CREATED, ctx, request, response));
    }

    /**
     * 使得上下文不可用，会最后调用destroy
     *
     * @param request
     * @param response
     */
    public void makeContextUnavailable(HttpServletRequest request, HttpServletResponse response) {
        handleRequestEnd(getRootTrace(), request, response);
        destroy();
        notifyListeners((listener, ctx) ->
            listener.onTraceContextChanged(ITraceContextListener.CONTEXT_CHANGE_TYPE_CONTEXT_DESTROYED, ctx, request, response));
    }

    /**
     * 获取根TraceObject
     *
     * @return 根TraceObject实例
     */
    public TraceObject getRootTrace() {
        return getRootObject();
    }

    /**
     * 获取当前方法调用的TraceObject
     *
     * @return 当前方法的TraceObject实例
     */
    public TraceObject getCurrentMethodTraceObject() {
        return getMethodInvokeTreeProvider().getCurrentTraceObject();
    }

    /**
     * 方法调用开始
     *
     * @param traceId 父级traceId
     * @param method 被调用的方法
     * @param args 方法参数
     * @param target 调用对象
     */
    public void beginMethod(String traceId, Method method, Object[] args, Object target) {
        MethodTraceObject traceObject =
            new MethodTraceObject(UuidUtil.getInstance().getUuidString(), method, args, target);
        getMethodInvokeTreeProvider().beginMethodFrom(traceId, traceObject);
        notifyListeners((listener, ctx) ->
            listener.onMethodInvokeBegin(traceObject, getMethodInvokeTreeProvider()));
    }

    /**
     * 方法调用结束
     *
     * @param traceId 当前方法的traceId
     */
    public void endMethod(String traceId) {
        TraceObject traceObject = getMethodInvokeTreeProvider().endMethod(traceId);
        notifyListeners((listener, ctx) ->
            listener.onMethodInvokeEnd(traceObject, getMethodInvokeTreeProvider()));
    }

    /**
     * 通知所有监听器
     *
     * @param action 监听器执行动作
     */
    private void notifyListeners(BiConsumer<ITraceContextListener, AbstractTraceContext> action) {
        if (CollUtil.isNotEmpty(getContextListeners())) {
            for (ITraceContextListener listener : getContextListeners()) {
                action.accept(listener, this);
            }
        }
    }
}
