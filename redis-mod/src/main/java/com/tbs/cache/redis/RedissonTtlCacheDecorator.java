package com.tbs.cache.redis;

import com.tbs.cache.ITtlCacheDecorator;
import com.tbs.cache.integration.AbstractTtlCache;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.helper.FeatureWrapperHelper;
import com.tbs.redis.config.property.RedissonCacheProperty;
import org.redisson.spring.cache.RedissonCache;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import javax.annotation.Resource;
import java.time.Duration;

/**
 * redisson缓存装饰器
 */
public class RedissonTtlCacheDecorator implements ITtlCacheDecorator {

    @Resource
    private RedissonCacheProperty redissonCacheProperty;

    @Override
    public AbstractTtlCache decorateCache(Cache cache, CacheManager manager) {
        var redissonCache = FeatureWrapperHelper.unwrap(cache, RedissonCache.class);
        if (redissonCache.isPresent()) {
            return new RedissonAdaptTtlCache(redissonCache.get(), cache,
                Duration.ofSeconds(redissonCacheProperty.getCacheTimeout()));
        } else {
            AssertUtil.throwError(() -> new IllegalArgumentException("cache is not redisson cache"));
            return null;
        }
    }

}
