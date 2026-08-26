package com.tbs.cache.policy;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * 默认缓存 TTL 策略实现
 * <p>
 * 支持全局默认 TTL 和按 cacheName 覆盖。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class DefaultCacheTtlPolicy implements ICacheTtlPolicy {

    /**
     * 全局默认 TTL
     */
    private final Duration defaultTtl;

    /**
     * 按 cacheName 的覆盖配置
     */
    private final Map<String, Duration> overrides;

    /**
     * 构造函数
     *
     * @param defaultTtl 全局默认 TTL
     * @param overrides  按 cacheName 的覆盖配置
     */
    public DefaultCacheTtlPolicy(Duration defaultTtl, Map<String, Duration> overrides) {
        this.defaultTtl = defaultTtl;
        this.overrides = overrides != null ? overrides : Map.of();
    }

    /**
     * 构造函数（仅指定默认 TTL）
     *
     * @param defaultTtl 全局默认 TTL
     */
    public DefaultCacheTtlPolicy(Duration defaultTtl) {
        this(defaultTtl, Map.of());
    }

    @Override
    public Duration ttlOf(String cacheName) {
        Duration override = overrides.get(cacheName);
        return override != null ? override : defaultTtl;
    }

    @Override
    public Optional<Duration> findOverride(String cacheName) {
        return Optional.ofNullable(overrides.get(cacheName));
    }

    @Override
    public Duration defaultTtl() {
        return defaultTtl;
    }
}
