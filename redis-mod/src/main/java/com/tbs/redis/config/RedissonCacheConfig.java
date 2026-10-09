package com.tbs.redis.config;

import com.tbs.cache.redis.RedissonLevelCacheProvider;
import com.tbs.redis.config.property.RedissonCacheProperty;
import org.redisson.api.RedissonClient;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.Resource;

/**
 * Redisson缓存配置类
 * <p>
 * 提供Redisson Spring Cache Manager的配置，用于集成Redisson缓存功能
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(RedissonCacheProperty.class)
public class RedissonCacheConfig {
    @Resource
    private RedissonCacheProperty property;

    /**
     * 创建Redisson Spring Cache Manager实例
     * <p>
     * 配置Redisson Spring Cache Manager，设置默认缓存配置、编解码器、是否允许空值和事务支持
     * 仅当不存在RedissonSpringCacheManager实例时创建
     * </p>
     *
     * @param redissonClient Redisson客户端实例
     * @return Redisson Spring Cache Manager实例
     */
    @Bean
    @ConditionalOnMissingBean(RedissonSpringCacheManager.class)
    public RedissonSpringCacheManager redisCacheManager(RedissonClient redissonClient) {
        var cacheManager = new RedissonSpringCacheManager(redissonClient) {
            /**
             * 创建默认缓存配置
             * <p>
             * 使用RedissonCacheProperty中的配置参数创建默认的CacheConfig
             * 设置缓存超时时间、最大空闲时间、最大大小和驱逐模式
             * </p>
             *
             * @return 默认缓存配置
             */
            @Override
            protected CacheConfig createDefaultConfig() {
                CacheConfig config = new CacheConfig(property.getCacheTimeout()*1000, property.getMaxIdleTime()*1000);
                config.setMaxSize(property.getMaxSize());
                config.setEvictionMode(property.getEvictionMode());
                return config;
            }

        };
        cacheManager.setCodec(redissonClient.getConfig().getCodec());
        cacheManager.setAllowNullValues(property.isAllowNullValues());
        cacheManager.setTransactionAware(property.isTransaction());
        return cacheManager;
    }

    /**
     * 创建 Redisson 层级缓存提供者（多级缓存 L2）
     * <p>
     * 仅在多级缓存启用（{@code tbs.cache.multi-level.enabled=true}）时注册，
     * 供多级缓存管理器收集组装层级。
     * </p>
     *
     * @param redissonClient Redisson 客户端实例
     * @return Redisson 层级缓存提供者
     */
    @Bean
    @ConditionalOnProperty(prefix = "tbs.cache.multi-level", name = "enabled", havingValue = "true")
    @ConditionalOnMissingBean(RedissonLevelCacheProvider.class)
    public RedissonLevelCacheProvider redissonLevelCacheProvider(RedissonSpringCacheManager cacheManager) {
        return new RedissonLevelCacheProvider(cacheManager);
    }

}
