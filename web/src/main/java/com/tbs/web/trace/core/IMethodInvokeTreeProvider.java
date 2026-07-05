package com.tbs.web.trace.core;

import com.tbs.struct.tree.api.ITree;

/**
 * 方法调用树提供者
 *
 * @author abstergo
 */
public interface IMethodInvokeTreeProvider {
    /**
     * 获取方法调用树
     *
     * @return 方法调用树实例
     */
    ITree<String, TraceObject> getTree();

    /**
     * 设置方法调用树根节点
     *
     * @param traceObject 根TraceObject
     */
    void setMethodRoot(TraceObject traceObject);

    /**
     * 开始方法调用
     *
     * @param parentTraceId 父级traceId
     * @param obj 当前方法的TraceObject
     */
    void beginMethodFrom(String parentTraceId, TraceObject obj);

    /**
     * 结束方法调用
     *
     * @param traceId 当前方法的traceId
     * @return 结束的方法的TraceObject
     */
    TraceObject endMethod(String traceId);

    /**
     * 获取当前方法调用对象
     *
     * @return 当前方法的TraceObject，如果不存在则返回null
     */
    TraceObject getCurrentTraceObject();
}
