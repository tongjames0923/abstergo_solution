package com.tbs.cache.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 多级缓存配置属性
 * <p>
 * 绑定 {@code tbs.cache.multi-level} 前缀的配置，控制多级缓存的启用与 TTL 策略。 不加 {@code @Component}，由 {@code MultiLevelCacheConfig} 通过
 * {@code @EnableConfigurationProperties} 统一注册，避免双重注册导致 Bean 冲突。
 * </p>
 * <p>
 * 配置示例：
 * <pre>
 * tbs:
 *   cache:
 *     multi-level:
 *       enabled: true
 *       ttl:
 *         default-ttl: 1h
 *         overrides:
 *           userCache: 30m
 *           dictCache: 24h
 * </pre>
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "tbs.cache.multi-level")
public class MultiLevelCacheProperty {

    /**
     * 是否启用多级缓存
     * <p>
     * 默认关闭，未启用时保持现有单级缓存行为不变。
     * </p>
     */
    private boolean enabled = false;

    /**
     * 全局默认 TTL
     * <p>
     * 未在 overrides 中声明的 cacheName 使用此值。
     * </p>
     */
    private Duration defaultTtl = Duration.ofHours(1);

}
