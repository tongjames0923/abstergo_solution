package com.tbs.web.trace.tool;

import com.tbs.web.trace.core.AbstractTraceContext;
import com.tbs.web.trace.core.IMethodInvokeTreeProvider;
import com.tbs.web.trace.core.TraceObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 链路跟踪环境监听器
 *
 * @author abstergo
 */
public interface ITraceContextListener {

    /**
     * 链路跟踪环境被创建
     */
    int CONTEXT_CHANGE_TYPE_CONTEXT_CREATED = 1;
    /**
     * 链路跟踪环境被销毁,通常是通过正确的方式销毁
     */
    int CONTEXT_CHANGE_TYPE_CONTEXT_DESTROYED = 2;
    /**
     * 链路跟踪环境被丢弃，通常是被错误方式销毁，如当请求处于有效的时候，却被重新构建环境
     */
    int CONTEXT_CHANGE_TYPE_CONTEXT_DROPPED = 3;

    /**
     * 链路跟踪环境变化
     *
     * @param changeType
     * @param traceContext
     * @param request
     * @param response
     */
    void onTraceContextChanged(int changeType, AbstractTraceContext traceContext, HttpServletRequest request,
        HttpServletResponse response);

    /**
     * 方法调用开始
     *
     * @param traceObject
     * @param provider
     */
    void onMethodInvokeBegin(TraceObject traceObject, IMethodInvokeTreeProvider provider);

    /**
     * 方法调用结束
     *
     * @param traceObject
     * @param provider
     */
    void onMethodInvokeEnd(TraceObject traceObject, IMethodInvokeTreeProvider provider);

}
