package com.tbs.trace.impl;

import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.struct.tree.api.ITree;
import com.tbs.struct.tree.api.ITreeNode;
import com.tbs.struct.tree.model.MultiwayTree;
import com.tbs.struct.tree.model.SimpleTreeNode;
import com.tbs.trace.core.IMethodInvokeTreeProvider;
import com.tbs.trace.core.TraceObject;

import java.util.Date;

/**
 * 简单方法调用树提供者（ThreadLocal，支持多线程）
 *
 * @author abstergo
 */
public class SimpleMethodInvokeTreeProvider implements IMethodInvokeTreeProvider {

    private final ThreadLocal<ITree<String, TraceObject>> treeHolder = ThreadLocal.withInitial(MultiwayTree::new);
    private final ThreadLocal<ITreeNode<String, TraceObject>> currentHolder = new ThreadLocal<>();

    @Override
    public ITree<String, TraceObject> getTree() {
        return treeHolder.get();
    }

    @Override
    public void setMethodRoot(TraceObject traceObject) {
        getTree().setRoot(new SimpleTreeNode<>(traceObject.getTraceId(), traceObject));
    }

    @Override
    public void beginMethodFrom(String parentTraceId, TraceObject obj) {
        ITreeNode<String, TraceObject> node =
            new SimpleTreeNode<>(obj.getTraceId(), obj.setParentTraceId(parentTraceId));
        currentHolder.set(node);
        getTree().insert(parentTraceId, node);
    }

    @Override
    public TraceObject endMethod(String traceId) {
        var now = getTree().find(traceId).orElseThrow(() -> new FormattedRuntimeException("can not find trace object"));
        now.setValue(now.getValue().setDisposeTime(new Date()).setValid(false));
        currentHolder.set(getTree().find(now.getValue().getParentTraceId()).orElse(null));
        return now.getValue();
    }

    @Override
    public TraceObject getCurrentTraceObject() {
        ITreeNode<String, TraceObject> cur = currentHolder.get();
        if (cur == null) {
            cur = getTree().getRoot();
        }
        return cur == null ? null : cur.getValue();
    }

    /**
     * 清理当前线程的trace数据（请求/Dubbo调用结束时调用）
     */
    public void clear() {
        treeHolder.remove();
        currentHolder.remove();
    }
}
