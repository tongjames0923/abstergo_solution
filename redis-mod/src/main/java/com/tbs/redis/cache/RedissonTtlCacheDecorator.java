package com.tbs.redis.cache;

import com.tbs.cache.ITtlCacheDecorator;
import com.tbs.cache.integration.AbstractTtlCache;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.FeatureWrapperHelper;
import org.redisson.spring.cache.RedissonCache;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

public class RedissonTtlCacheDecorator implements ITtlCacheDecorator {
    @Override
    public AbstractTtlCache decorateCache(Cache cache, CacheManager manager) {
        AssertUtil.notNull(cache, "Cache cannot be null");
        AssertUtil.notNull(manager, "CacheManager cannot be null");
        var redissonCache = FeatureWrapperHelper.unwrap(cache, RedissonCache.class);
        AssertUtil.isTrue(redissonCache.isEmpty(), "Cache is not a RedissonCache");
        // TODO: 待实现
        return null;
    }
}
