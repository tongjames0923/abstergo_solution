package com.tbs.cache.impl;

import com.tbs.cache.ITtlCacheDecorator;
import com.tbs.cache.integration.AbstractTtlCache;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

public class DefaultTtlCacheDecorator implements ITtlCacheDecorator {
    @Override
    public AbstractTtlCache decorateCache(Cache cache, CacheManager manager) {
        return new AdaptScheduleExpireTtlCache(cache);
    }
}
