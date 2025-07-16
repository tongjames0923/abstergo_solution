package com.tbs.web.trace.core;

import com.tbs.common.tree.ITree;

/**
 * 方法调用树提供者
 *
 * @author abstergo
 */
public interface IMethodInvokeTreeProvider {
    /**
     * 获取方法调用树
     *
     * @return
     */
    ITree<String, TraceObject> getTree();

    /**
     * 设置方法调用树根节点
     *
     * @param traceObject
     */
    void setMethodRoot(TraceObject traceObject);

    /**
     * 开始方法调用
     *
     * @param parentTraceId
     * @param obj
     */
    void beginMethodFrom(String parentTraceId, TraceObject obj);

    /**
     * 结束方法调用
     *
     * @param traceId
     * @return
     */
    TraceObject endMethod(String traceId);

    /**
     * 获取当前方法调用对象
     *
     * @return
     */
    TraceObject getCurrentTraceObject();
}
