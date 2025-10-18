package com.tbs.redis.config;

import com.tbs.redis.config.property.RedissonCacheProperty;
import org.redisson.api.RedissonClient;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

public class RedissonCacheConfig {

    @Bean
    @ConditionalOnMissingBean(RedissonSpringCacheManager.class)
    public RedissonSpringCacheManager redisCacheManager(RedissonClient redissonClient, RedissonCacheProperty property) {
        var cacheManager = new RedissonSpringCacheManager(redissonClient);
        cacheManager.setAllowNullValues(property.isAllowNullValues());
        cacheManager.setTransactionAware(property.isTransaction());
        return cacheManager;
    }
}
