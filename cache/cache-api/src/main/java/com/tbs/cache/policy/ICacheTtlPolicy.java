package com.tbs.cache.policy;

import java.time.Duration;
import java.util.Optional;

/**
 * 缓存 TTL 策略接口
 * <p>
 * 定义缓存过期时间的策略，是 TTL 的唯一事实来源。
 * 支持按 cacheName 配置不同的 TTL。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface ICacheTtlPolicy {

    /**
     * 获取指定 cacheName 的 TTL
     * <p>
     * 如果存在覆盖配置，返回覆盖值；否则返回默认值。
     * </p>
     *
     * @param cacheName 缓存名称（不带前缀）
     * @return TTL，永不过期返回 null
     */
    Duration ttlOf(String cacheName);

    /**
     * 查找指定 cacheName 的 TTL 覆盖配置
     * <p>
     * 仅返回显式配置的值，不回退到默认值。
     * </p>
     *
     * @param cacheName 缓存名称（不带前缀）
     * @return 覆盖配置的 TTL，无覆盖返回 empty
     */
    Optional<Duration> findOverride(String cacheName);

    /**
     * 获取默认 TTL
     *
     * @return 默认 TTL，永不过期返回 null
     */
    Duration defaultTtl();
}
