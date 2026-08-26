package com.tbs.cache.redis;

import com.tbs.cache.ITtlCacheDecorator;
import com.tbs.cache.integration.AbstractTtlCache;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.helper.FeatureWrapperHelper;
import com.tbs.redis.config.property.RedissonCacheProperty;
import org.redisson.spring.cache.RedissonCache;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Duration;

/**
 * redisson缓存装饰器
 */
@Component
public class RedissonTtlCacheDecorator implements ITtlCacheDecorator {

    @Resource
    private RedissonCacheProperty redissonCacheProperty;

    /**
     * 判断当前缓存是否支持TTL功能
     * <p>
     * 由于Spring Cache的限制，无法直接在Cache接口层面添加TTL功能 因此需要通过Cache的实现类或者CacheManager的实现类进行判断
     * </p>
     *
     * @param cache   要判断的缓存实例
     * @param manager 缓存管理器
     * @return 如果当前缓存支持TTL功能则返回true，否则返回false
     */
    @Override
    public boolean support(Cache cache, CacheManager manager) {
        return FeatureWrapperHelper.unwrap(cache, RedissonCache.class).isPresent();
    }

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

    /**
     * @return
     */
    @Override
    public int getOrder() {
        return 1000;
    }
}
