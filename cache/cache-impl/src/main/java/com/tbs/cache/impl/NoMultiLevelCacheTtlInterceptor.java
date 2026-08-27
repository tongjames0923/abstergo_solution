package com.tbs.cache.impl;

import com.tbs.cache.integration.AbstractTtlCache;

import java.util.Date;

public class NoMultiLevelCacheTtlInterceptor implements MultiLevelAdaptTtlCache.IMultiLevelCacheTtlInterceptor {
    /**
     * 拦截缓存过期时间
     *
     * @param cache
     * @param level
     * @param key
     * @param expireTime
     * @return
     */
    @Override
    public Date intercept(AbstractTtlCache cache, int level, Object key, Date expireTime) {
        return null;
    }
}
