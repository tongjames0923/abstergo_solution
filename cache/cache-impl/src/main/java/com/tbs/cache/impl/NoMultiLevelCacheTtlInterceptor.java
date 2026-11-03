package com.tbs.cache.impl;

import org.springframework.cache.Cache;

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
    public Date intercept(Cache cache, int level, Object key, Date expireTime) {
        return expireTime;
    }
}
