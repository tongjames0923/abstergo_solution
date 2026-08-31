package com.tbs.cache.integration;

import org.springframework.cache.Cache;

import java.time.Duration;
import java.util.Map;

/**
 * 层级缓存提供者 SPI 接口
 * <p>
 * 定义缓存层级提供者的标准接口，用于构建多级缓存架构。
 * 每个实现代表一个缓存层级（如 L1 Caffeine、L2 Redisson）。
 * </p>
 * <p>
 * 实现要求：
 * <ul>
 *   <li>通过 {@link #getOrder()} 确定层级顺序（值越小越靠近应用层）</li>
 *   <li>通过 {@link #support(String)} 判断是否支持指定 cacheName</li>
 *   <li>通过 {@link #getCache(String)} 获取或创建缓存实例</li>
 * </ul>
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface ILevelCacheProvider {

    /**
     * 获取层级顺序
     * <p>
     * 值越小越靠近应用层（如 L1=0，L2=1）。
     * 读取时按此顺序穿透，写入时按此顺序回填。
     * </p>
     *
     * @return 层级顺序，默认返回当前实例的 hashCode
     */
    default int getOrder() {
        return 0;
    }

    /**
     * 判断是否支持指定 cacheName
     * <p>
     * 用于过滤不支持的缓存名称。默认全部支持。
     * </p>
     *
     * @param cacheName 缓存名称
     * @return 支持返回 true，否则返回 false
     */
    default boolean support(String cacheName) {
        return true;
    }

    /**
     * 获取或创建缓存实例
     *
     * @param name 缓存名称
     * @return 缓存实例
     */
    Cache getCache(String name);

    /**
     * 获取或创建带 TTL 的缓存实例
     * <p>
     * 默认实现委托给 {@link #getCache(String)}。
     * 子类可覆写以实现 per-cacheName TTL 绑定。
     * </p>
     *
     * @param name 缓存名称
     * @param ttl  业务 TTL，可能为 null
     * @return 缓存实例
     */
    default Cache getCache(String name, Duration ttl) {
        return getCache(name);
    }

    /**
     * 配置提供者
     * <p>
     * 用于从外部传入配置参数（如 Caffeine 的 maximumSize、Redisson 的 maxSize 等）。
     * 默认空实现。
     * </p>
     *
     * @param props 配置参数
     */
    default void configure(Map<String, Object> props) {
        // no-op
    }
}
