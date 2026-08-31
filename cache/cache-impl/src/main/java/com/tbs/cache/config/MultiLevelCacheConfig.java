package com.tbs.cache.config;

import com.tbs.cache.ITtlCacheDecorator;
import com.tbs.cache.impl.MultiLevelAdaptTtlCache;
import com.tbs.cache.impl.MultiLevelTtlCacheDecorator;
import com.tbs.cache.impl.NoMultiLevelCacheTtlInterceptor;
import com.tbs.cache.policy.DefaultCacheTtlPolicy;
import com.tbs.cache.policy.ICacheTtlPolicy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 多级缓存装配配置
 * <p>
 * 负责注册 {@link MultiLevelCacheProperty} 与多级缓存启用时的 {@link ICacheTtlPolicy} Bean。
 * 多级 TTL 缓存管理器本身由 {@link TbsCacheConfig#adaptCacheMananger} 根据
 * {@code tbs.cache.multi-level.enabled} 开关切换创建，保证容器内唯一 {@code @Primary} 管理器。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(MultiLevelCacheProperty.class)
public class MultiLevelCacheConfig {

    /**
     * 创建缓存 TTL 策略
     * <p>
     * 仅在多级缓存启用时注册。业务未提供自定义 {@link ICacheTtlPolicy} 时，
     * 使用配置属性构建默认策略（全局默认值 + per-cacheName 覆盖）。
     * </p>
     *
     * @param property 多级缓存配置属性
     * @return 缓存 TTL 策略
     */
    @Bean
    @ConditionalOnProperty(prefix = "tbs.cache.multi-level", name = "enabled", havingValue = "true")
    @ConditionalOnMissingBean(ICacheTtlPolicy.class)
    ICacheTtlPolicy cacheTtlPolicy(MultiLevelCacheProperty property, TbsCacheProperty tbsCacheProperty) {
        return new DefaultCacheTtlPolicy(property.getDefaultTtl(), tbsCacheProperty.getTtl().getOverrides());
    }

    @Bean
    @ConditionalOnProperty(prefix = "tbs.cache.multi-level", name = "enabled", havingValue = "true")
    ITtlCacheDecorator multiLevelTtlCacheDecorator() {
        return new MultiLevelTtlCacheDecorator();
    }

    @Bean
    @ConditionalOnMissingBean(MultiLevelAdaptTtlCache.IMultiLevelCacheTtlInterceptor.class)
    MultiLevelAdaptTtlCache.IMultiLevelCacheTtlInterceptor defaultMultiLevelCacheTtlInterceptor() {
        return new NoMultiLevelCacheTtlInterceptor();
    }
}
