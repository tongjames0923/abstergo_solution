package com.tbs.cache.redis;

import com.tbs.cache.integration.ILevelCacheProvider;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.redisson.spring.cache.RedissonCache;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.cache.Cache;

import java.time.Duration;

/**
 * Redisson 层级缓存提供者（多级缓存 L2）
 * <p>
 * 实现 {@link ILevelCacheProvider}，基于 {@link RedissonClient} 的
 * {@link RMapCache} 提供带原生 TTL 的分布式缓存层。
 * 每个 cacheName 对应独立的 {@link RedissonCache} 实例，创建时绑定
 * 业务 TTL（取业务 TTL 与 {@code tbs.redission.cache.cacheTimeout} 的较小值）。
 * </p>
 * <p>
 * 不直接标注 {@code @Component}，由 {@code RedissonCacheConfig} 在多级缓存
 * 启用时按条件注册为 Bean，避免未启用时产生无效实例。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
public class RedissonLevelCacheProvider implements ILevelCacheProvider {

    private final RedissonSpringCacheManager springCacheManager;

    /**
     * 构造函数
     *
     * @param redissonClient Redisson 客户端实例
     * @param property       Redisson 缓存配置属性
     */
    public RedissonLevelCacheProvider(RedissonSpringCacheManager springCacheManager) {
        this.springCacheManager = springCacheManager;
    }

    /**
     * 获取层级顺序
     * <p>
     * Redis 作为分布式共享层，慢于进程内缓存（如 Caffeine order=0），
     * 与 {@link RedissonTtlCacheDecorator#getOrder()} 保持一致。
     * </p>
     *
     * @return 层级顺序 1000
     */
    @Override
    public int getOrder() {
        return 1000;
    }

    /**
     * 获取或创建缓存实例（使用全局默认 TTL）
     *
     * @param name 缓存名称
     * @return 缓存实例
     */
    @Override
    public Cache getCache(String name) {
        return getCache(name, null);
    }

    /**
     * 获取或创建带 TTL 的缓存实例
     * <p>
     * 按 cacheName 缓存实例，同一 cacheName 只创建一次。
     * </p>
     *
     * @param name 缓存名称
     * @param ttl  业务 TTL，可为 null（使用全局默认）
     * @return 缓存实例
     */
    @Override
    public Cache getCache(String name, Duration ttl) {
        log.warn("redisson level cache provider can not set ttl by businessTtl please use @ExpireOn");
        return springCacheManager.getCache(name);
    }

}
