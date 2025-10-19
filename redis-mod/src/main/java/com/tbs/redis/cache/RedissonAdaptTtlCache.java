package com.tbs.redis.cache;

import com.tbs.cache.integration.AbstractAdaptTtlCache;
import com.tbs.common.util.helper.FeatureWrapperHelper;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RMapCache;
import org.redisson.spring.cache.RedissonCache;
import org.springframework.cache.Cache;

import java.time.Duration;
import java.util.Date;

@Slf4j
public class RedissonAdaptTtlCache extends AbstractAdaptTtlCache {

    private Duration defaultExpireDuration;

    /**
     * 构造函数，初始化原始缓存
     *
     * @param orginCache 原始Spring Cache实例
     */
    public RedissonAdaptTtlCache(RedissonCache orginCache, Duration defaultExpireDuration) {
        super(orginCache);
        this.defaultExpireDuration = defaultExpireDuration == null ? Duration.ofHours(1) : defaultExpireDuration;
    }

    @Override
    public boolean expireAll(Cache cache, Date expireTime) {
        var redissonCache = FeatureWrapperHelper.unwrap(cache, RedissonCache.class);
        if (redissonCache.isPresent()) {
            return redissonCache.get().getNativeCache().expire(expireTime == null ? defaultExpireDuration
                : Duration.between(new Date().toInstant(), expireTime.toInstant()));
        }
        log.warn("RedissonCache not support expireAll");
        return false;
    }

    @Override
    public boolean expire(@NotNull Cache cache, Object key, @NotNull Date expireTime) {
        var redissonCache = FeatureWrapperHelper.unwrap(cache, RedissonCache.class);
        if (redissonCache.isPresent()) {
            var nativeCache = redissonCache.get().getNativeCache();
            if (nativeCache instanceof RMapCache) {
                var t = expireTime.getTime() - System.currentTimeMillis();
                if (t <= 0) {
                    return ((RMapCache)nativeCache).remove(key) != null;
                }
                return ((RMapCache)nativeCache).expireEntry(key, Duration.ofMillis(t), Duration.ZERO);
            }

        }
        log.warn("RedissonCache not support expire");
        return false;
    }

}
