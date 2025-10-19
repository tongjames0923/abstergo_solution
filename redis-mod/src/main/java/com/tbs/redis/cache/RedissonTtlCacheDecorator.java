package com.tbs.redis.cache;

import com.tbs.cache.ITtlCacheDecorator;
import com.tbs.cache.integration.AbstractTtlCache;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.helper.FeatureWrapperHelper;
import com.tbs.redis.config.property.RedissonCacheProperty;
import org.redisson.spring.cache.RedissonCache;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheDecorator;

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
        if (cache instanceof TransactionAwareCacheDecorator) {
            return decorateCache(((TransactionAwareCacheDecorator)(cache)).getTargetCache(), manager);
        }
        var redissonCache = FeatureWrapperHelper.unwrap(cache, RedissonCache.class);

        if (redissonCache.isPresent()) {
            return new RedissonAdaptTtlCache(redissonCache.get(), Duration.ofSeconds(redissonCacheProperty.getCacheTimeout()));
        } else {
            AssertUtil.throwError(() -> new IllegalArgumentException("cache is not redisson cache"));
            return null;
        }
    }

}
