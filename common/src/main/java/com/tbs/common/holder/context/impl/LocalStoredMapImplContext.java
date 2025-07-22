package com.tbs.common.holder.context.impl;

import com.tbs.common.holder.context.AbstractLocalStroreNamedContext;
import com.tbs.common.holder.context.IContext;
import com.tbs.common.error.AssertUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 本地Map存储上下文
 *
 * @author abstergo
 */
public class LocalStoredMapImplContext<V> extends AbstractLocalStroreNamedContext<String, V> {
    /**
     * 本地存储上下文ID
     */
    public static final String LOCAL_STORED_CONTEXT_ID = "local-stored-context";

    private Map<String, V> map;

    /**
     * 构造函数
     */
    public LocalStoredMapImplContext() {
        this(LOCAL_STORED_CONTEXT_ID, new HashMap<>(16));
    }

    public LocalStoredMapImplContext(String contextId) {
        this(contextId, new HashMap<>(16));
    }

    @Override
    protected IContext<V> rebuild() {
        return this;
    }

    /**
     * 构造函数
     *
     * @param map 存储对象
     */
    public LocalStoredMapImplContext(Map<String, V> map) {
        this(LOCAL_STORED_CONTEXT_ID, map);
    }

    /**
     * 构造函数
     *
     * @param contextId 上下文ID
     * @param map       存储对象
     */
    public LocalStoredMapImplContext(String contextId, Map<String, V> map) {
        super(contextId);
        AssertUtil.notNull(map, "map can not be null");
        this.map = map;
    }

    @Override
    protected Map<String, V> getMap() {
        return map;
    }

}
