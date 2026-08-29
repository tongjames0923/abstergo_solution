package com.tbs.cache.caffeine.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Caffeine 缓存配置属性
 * <p>
 * 绑定 {@code tbs.cache.level.caffeine} 前缀的配置，定义 Caffeine 缓存的 全局配置参数（容量、过期、统计、空值策略）。常规单级与多级场景共享此配置： 常规场景用于
 * {@code CaffeineCacheManager} 的构建参数， 多级场景下每个 cacheName 创建独立的 Caffeine 实例并共享这些全局配置。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "tbs.cache.level.caffeine")
public class CaffeineLevelCacheProperty {

    /**
     * 每个缓存实例的最大条目数
     * <p>
     * 超出后按 LRU 策略淘汰。
     * </p>
     */
    private long maximumSize = 10_000;

    /**
     * 全局默认写入过期时间
     * <p>
     * 常规场景作为缓存级原生 TTL； 多级场景作为 L1 的安全网上限：实际 TTL = min(全局, 业务 TTL)， 确保 L1 不会比 L2 存活更久，保证最终一致性。
     * </p>
     */
    private Duration expireAfterWrite = Duration.ofSeconds(60);

    /**
     * 是否启用统计记录
     * <p>
     * 启用后可通过 {@code cache.stats()} 获取命中率等指标。 会有轻微性能开销。
     * </p>
     */
    private boolean recordStats = false;

    /**
     * 是否允许缓存 null 值
     * <p>
     * 设为 true 时，{@code cache.put(key, null)} 不会抛 NPE， 而是缓存一个特殊的 null 标记。
     * </p>
     */
    private boolean allowNullValues = true;
}
