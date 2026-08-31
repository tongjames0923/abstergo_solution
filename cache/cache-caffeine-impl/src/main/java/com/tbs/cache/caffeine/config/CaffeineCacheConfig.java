package com.tbs.cache.caffeine.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.tbs.cache.caffeine.config.property.CaffeineLevelCacheProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Caffeine 常规（单级）缓存配置
 * <p>
 * 提供按 {@link CaffeineLevelCacheProperty} 预配置的 {@link CaffeineCacheManager} Bean， 供常规单级缓存场景使用：将
 * {@code tbs.cache.cache-manager-impl} 配置为 {@code org.springframework.cache.caffeine.CaffeineCacheManager} 后，
 * {@code DefaultTtlAdaptCacheMananger} 按类型查找即命中本实例， 各 {@link org.springframework.cache.caffeine.CaffeineCache} 再由
 * {@code CaffeineTtlCacheDecorator} 装饰为 TTL 缓存。
 * </p>
 * <p>
 * 与多级缓存（{@code CaffeineLevelCacheProvider}）相互独立： 多级启用时本 Bean 不参与编排，仅作常规降级可用。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(CaffeineLevelCacheProperty.class)
public class CaffeineCacheConfig {

    /**
     * 创建常规 Caffeine 缓存管理器
     * <p>
     * 应用配置的最大容量与 {@code expireAfterWrite} 缓存级原生 TTL， 动态缓存按需创建（不预注册缓存名）。业务已自定义 {@link CaffeineCacheManager} Bean
     * 时让位于业务实现。
     * </p>
     *
     * @param property Caffeine 配置属性
     * @return 配置好的 Caffeine 缓存管理器
     */
    @Bean
    @ConditionalOnMissingBean(CaffeineCacheManager.class)
    public CaffeineCacheManager caffeineCacheManager(CaffeineLevelCacheProperty property) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        Caffeine<Object, Object> builder = Caffeine.newBuilder().maximumSize(property.getMaximumSize())
            .expireAfterWrite(property.getExpireAfterWrite());
        if (property.isRecordStats()) {
            builder.recordStats();
        }
        cacheManager.setCaffeine(builder);
        cacheManager.setAllowNullValues(property.isAllowNullValues());
        return cacheManager;
    }
}
