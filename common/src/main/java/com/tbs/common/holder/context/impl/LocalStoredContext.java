package com.tbs.common.holder.context.impl;

import com.tbs.common.holder.context.AbstractNamedContext;

import java.util.HashMap;
import java.util.Map;

/**
 * 本地存储上下文
 *
 * @author abstergo
 */
public class LocalStoredContext extends AbstractNamedContext {
    /**
     * 本地存储上下文ID
     */
    public static final String LOCAL_STORED_CONTEXT_ID = "local-stored-context";

    /**
     * 本地存储
     */
    private Map<String, Object> storage = new HashMap<>();

    /**
     * 构造函数
     */
    public LocalStoredContext() {
        this(LOCAL_STORED_CONTEXT_ID);
    }

    /**
     * 构造函数
     *
     * @param contextId 上下文ID
     */
    public LocalStoredContext(String contextId) {
        super(contextId);
    }

    @Override
    public Object get(String key) {
        return storage.get(key);
    }

    @Override
    public void set(String key, Object value) {
        storage.put(key, value);
    }

    @Override
    public void remove(String key) {
        storage.remove(key);
    }
}
