package com.tbs.cache.impl.manager;

/**
 * @author abstergo
 */
public class SimpleCcontextCacheManager extends AbstractContextCacheManager {
    public SimpleCcontextCacheManager(String cacheNameContextId, String cacheContextId, boolean acceptNull) {
        super(cacheNameContextId, cacheContextId, acceptNull);
    }

    @Override
    protected String selectCacheName(String name) {
        return getCacheContext().getContextId();
    }
}
