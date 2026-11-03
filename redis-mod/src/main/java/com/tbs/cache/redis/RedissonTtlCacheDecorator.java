package com.tbs.cache.redis;

import com.tbs.cache.ITtlCacheDecorator;
import com.tbs.cache.integration.AbstractTtlCache;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.helper.FeatureWrapperHelper;
import com.tbs.redis.config.property.RedissonCacheProperty;
import jakarta.annotation.Resource;
import org.redisson.spring.cache.RedissonCache;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

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

    /**
     * 将 Redisson 缓存装饰为支持 TTL 的缓存
     * <p>
     * 传入的是原始入参 {@code cache}（保留 {@code TransactionAwareCacheDecorator} 等外层包装），
     * unwrap 结果只用于判定是否命中 Redisson 引擎，不作为被包装对象。
     * </p>
     *
     * @param cache   要装饰的缓存实例
     * @param manager 缓存管理器
     * @return 装饰后的 TTL 缓存实例，非 Redisson 缓存时断言报错
     */
    @Override
    public AbstractTtlCache decorateCache(Cache cache, CacheManager manager) {
        var redissonCache = FeatureWrapperHelper.unwrap(cache, RedissonCache.class);
        if (redissonCache.isPresent()) {
            return new RedissonAdaptTtlCache(cache, Duration.ofSeconds(redissonCacheProperty.getCacheTimeout()));
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
