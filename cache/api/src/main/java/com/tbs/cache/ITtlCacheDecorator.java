package com.tbs.cache;

import com.tbs.cache.integration.AbstractTtlCache;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * TTL缓存装饰器接口
 *
 * @author tongj
 */
public interface ITtlCacheDecorator {
    /**
     * 装饰缓存成Ttl缓存
     *
     * @param cache
     * @param manager
     * @return
     */
    AbstractTtlCache decorateCache(Cache cache, CacheManager manager);
}
