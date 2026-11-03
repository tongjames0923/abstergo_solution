package com.tbs.cache.redis;

import com.tbs.cache.integration.AbstractAdaptTtlCache;
import lombok.extern.slf4j.Slf4j;
import org.redisson.spring.cache.RedissonCache;

import java.time.Duration;

@Slf4j
public class RedissonAdaptTtlCache extends AbstractAdaptTtlCache {

    private RedissonCache cache;

    /**
     * 构造函数，初始化原始缓存
     *
     * @param orginCache 原始Spring Cache实例
     */
    public RedissonAdaptTtlCache(RedissonCache orginCache, Duration defaultExpireDuration) {
        super(orginCache, new RedissionCacheExpiredActuator(
            defaultExpireDuration != null ? defaultExpireDuration : Duration.ofSeconds(60)));
    }
}
