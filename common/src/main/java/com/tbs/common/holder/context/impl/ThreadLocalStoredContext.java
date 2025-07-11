package com.tbs.common.holder.context.impl;

import com.tbs.common.holder.AbstractMapImplDictionary;
import com.tbs.common.holder.IDictionary;
import com.tbs.common.holder.context.AbstractNamedContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    private ThreadLocal<IDictionary<String, Object>> threadLocal = new InheritableThreadLocal<>();


    @Override
    public IDictionary<String, Object> getSource() {
        if (threadLocal.get() == null) {
            threadLocal.set(new AbstractMapImplDictionary<>() {
                private Map<String, Object> map = new ConcurrentHashMap<>(8);

                @Override
                protected Map<String, Object> getMap() {
                    return map;
                }
            });
        }

        return threadLocal.get();
    }
}
