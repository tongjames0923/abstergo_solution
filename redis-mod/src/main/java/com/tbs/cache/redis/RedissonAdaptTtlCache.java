package com.tbs.cache.redis;

import com.tbs.cache.integration.AbstractAdaptTtlCache;
import com.tbs.common.util.helper.FeatureWrapperHelper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapCache;
import org.redisson.spring.cache.RedissonCache;
import org.springframework.cache.Cache;

import java.time.Duration;
import java.util.Date;

@Slf4j
public class RedissonAdaptTtlCache extends AbstractAdaptTtlCache {

    private Duration defaultExpireDuration;
    private RedissonCache cache;

    /**
     * 构造函数，初始化原始缓存
     *
     * @param orginCache 原始Spring Cache实例
     */
    public RedissonAdaptTtlCache(RedissonCache orginCache, Cache orgin, Duration defaultExpireDuration) {
        super(orgin);
        this.defaultExpireDuration = defaultExpireDuration == null ? Duration.ofHours(1) : defaultExpireDuration;
        this.cache = orginCache;
    }

    @Override
    public boolean expireAll(Cache cache, Date expireTime) {
        var redissonCache = FeatureWrapperHelper.unwrap(cache, RedissonCache.class);
        if (redissonCache.isPresent()) {
            return redissonCache.get().getNativeCache().expire(expireTime == null ? defaultExpireDuration
                : Duration.between(new Date().toInstant(), expireTime.toInstant())) &&
                super.expireAll(cache, expireTime);
        }
        log.warn("this cache type {} not support expireAll by RedissonAdaptTtlCache", cache.getClass().getName());
        return false;
    }

    @Override
    public boolean expire( Cache cache, Object key,  Date expireTime) {
        var redissonCache = FeatureWrapperHelper.unwrap(cache, RedissonCache.class);
        if (redissonCache.isPresent()) {
            var nativeCache = redissonCache.get().getNativeCache();
            if (nativeCache instanceof RMapCache) {
                var t = expireTime.getTime() - System.currentTimeMillis();
                if (t <= 0) {
                    return ((RMapCache)nativeCache).remove(key) != null && super.expire(cache, key, expireTime);
                }
                return ((RMapCache)nativeCache).expireEntry(key, Duration.ofMillis(t), Duration.ZERO) &&
                    super.expire(cache, key, expireTime);
            }

        }
        log.warn("this cache type {} not support expire by RedissonAdaptTtlCache", cache.getClass().getName());
        return false;
    }

}
