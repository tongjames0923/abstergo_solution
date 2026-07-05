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
 * 简单方法调用树提供者
 *
 * @author abstergo
 */
public class SimpleMethodInvokeTreeProvider implements IMethodInvokeTreeProvider {
    private ITree<String, TraceObject> tree = new MultiwayTree<>();

    private ITreeNode<String, TraceObject> current = null;

    @Override
    public ITree<String, TraceObject> getTree() {
        return tree;
    }

    @Override
    public void setMethodRoot(TraceObject traceObject) {
        getTree().setRoot(new SimpleTreeNode<>(traceObject.getTraceId(), traceObject));
    }

    @Override
    public void beginMethodFrom(String parentTraceId, TraceObject obj) {
        current = new SimpleTreeNode<>(obj.getTraceId(), obj.setParentTraceId(parentTraceId));
        getTree().insert(parentTraceId, current);
    }

    @Override
    public TraceObject endMethod(String traceId) {
        var now = getTree().find(traceId).orElseThrow(() -> new FormattedRuntimeException("can not find trace object"));
        now.setValue(now.getValue().setDisposeTime(new Date()).setValid(false));
        current = getTree().find(now.getValue().getParentTraceId()).orElse(null);
        return now.getValue();
    }

    @Override
    public TraceObject getCurrentTraceObject() {
        ITreeNode<String, TraceObject> cur = current == null ? getTree().getRoot() : current;
        return cur == null ? null : cur.getValue();
    }
}
