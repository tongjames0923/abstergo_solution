package com.tbs.cache.caffeine.config;

import com.tbs.cache.caffeine.config.property.CaffeineLevelCacheProperty;
import com.tbs.cache.caffeine.level.CaffeineLevelCacheProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Caffeine 多级缓存（L1）装配配置
 * <p>
 * 通过 {@code tbs.cache.level.caffeine.*} 配置属性，
 * 自动创建 {@link CaffeineLevelCacheProvider} Bean，
 * 供多级缓存管理器收集组装 L1 层级。
 * </p>
 * <p>
 * 配置示例：
 * <pre>
 * tbs:
 *   cache:
 *     level:
 *       caffeine:
 *         maximum-size: 10000
 *         expire-after-write: 60s
 *         record-stats: false
 *         allow-null-values: false
 * </pre>
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(CaffeineLevelCacheProperty.class)
public class CaffeineLevelCacheConfig {

    /**
     * 创建 Caffeine L1 层级缓存提供者
     * <p>
     * 业务已自定义 {@link CaffeineLevelCacheProvider} Bean 时让位于业务实现。
     * </p>
     *
     * @param property Caffeine 配置属性
     * @return 层级缓存提供者
     */
    @Bean
    @ConditionalOnMissingBean(CaffeineLevelCacheProvider.class)
    public CaffeineLevelCacheProvider caffeineLevelCacheProvider(CaffeineLevelCacheProperty property) {
        return new CaffeineLevelCacheProvider(property);
    }
}
