package com.tbs.common.holder.context.impl;

import com.tbs.common.holder.AbstractMapImplDictionary;
import com.tbs.common.holder.IDictionary;
import com.tbs.common.holder.context.AbstractNamedContext;

import java.util.HashMap;
import java.util.Map;

/**
 * 本地Map存储上下文
 *
 * @author abstergo
 */
public class LocalStoredMapImplContext extends AbstractNamedContext {
    /**
     * 本地存储上下文ID
     */
    public static final String LOCAL_STORED_CONTEXT_ID = "local-stored-context";

    private IDictionary<String, Object> source = new AbstractMapImplDictionary<String, Object>() {
        private Map<String, Object> map = new HashMap<>();

        @Override
        protected Map<String, Object> getMap() {
            return map;
        }
    };

    /**
     * 构造函数
     */
    public LocalStoredMapImplContext() {
        this(LOCAL_STORED_CONTEXT_ID);
    }

    /**
     * 构造函数
     *
     * @param contextId 上下文ID
     */
    public LocalStoredMapImplContext(String contextId) {
        super(contextId);
    }

    @Override
    public IDictionary<String, Object> getSource() {
        return source;
    }
}
