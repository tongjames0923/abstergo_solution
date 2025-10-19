package com.tbs.redis.config;

import com.tbs.redis.config.property.RedissonCacheProperty;
import org.redisson.api.RedissonClient;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import javax.annotation.Resource;

public class RedissonCacheConfig {
    @Resource
    private RedissonCacheProperty property;

    @Bean
    @ConditionalOnMissingBean(RedissonSpringCacheManager.class)
    public RedissonSpringCacheManager redisCacheManager(RedissonClient redissonClient) {
        var cacheManager = new RedissonSpringCacheManager(redissonClient) {
            @Override
            protected CacheConfig createDefaultConfig() {
                CacheConfig config = new CacheConfig(property.getCacheTimeout(), property.getMaxIdleTime());
                config.setMaxSize(property.getMaxSize());
                config.setEvictionMode(property.getEvictionMode());
                return config;
            }
            
        };
        cacheManager.setAllowNullValues(property.isAllowNullValues());
        cacheManager.setTransactionAware(property.isTransaction());
        return cacheManager;
    }
}
