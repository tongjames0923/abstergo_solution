package com.tbs.common.holder.context.impl;

import com.tbs.common.holder.context.AbstractNamedContext;

import java.util.HashMap;
import java.util.Map;

/**
 * 线程存储上下文
 *
 * @author abstergo
 */
public class ThreadLocalStoredContext extends AbstractNamedContext {

    public static final String THREAD_LOCAL_STORED_CONTEXT_ID = "thread-local-stored-context";

    /**
     * 创建一个线程存储上下文
     *
     * @param contextId 上下文ID
     */
    public ThreadLocalStoredContext(String contextId) {
        super(contextId);
    }

    /**
     * 创建一个线程存储上下文
     */
    public ThreadLocalStoredContext() {
        super(THREAD_LOCAL_STORED_CONTEXT_ID);
    }

    /**
     * 线程存储上下文
     */
    private ThreadLocal<HashMap<String, Object>> threadLocal = new InheritableThreadLocal<>();

    /**
     * 获取存储的Map
     *
     * @return 存储的Map
     */
    private Map<String, Object> getMap() {
        if (threadLocal.get() == null) {
            threadLocal.set(new HashMap<>());
        }
        return threadLocal.get();
    }

    @Override
    public Object get(String key) {
        return getMap().get(key);
    }

    @Override
    public void set(String key, Object value) {
        getMap().put(key, value);
    }

    @Override
    public void remove(String key) {
        getMap().remove(key);
    }
}
