package com.tbs.cache.redis;

import com.tbs.cache.integration.ILevelCacheProvider;
import com.tbs.redis.config.property.RedissonCacheProperty;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonCache;
import org.springframework.cache.Cache;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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

    /**
     * Redisson 客户端
     */
    private final RedissonClient redissonClient;

    /**
     * Redisson 缓存配置属性
     */
    private final RedissonCacheProperty property;

    /**
     * cacheName → RedissonCache 实例缓存
     */
    private final ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<>(16);

    /**
     * 构造函数
     *
     * @param redissonClient Redisson 客户端实例
     * @param property       Redisson 缓存配置属性
     */
    public RedissonLevelCacheProvider(RedissonClient redissonClient, RedissonCacheProperty property) {
        this.redissonClient = redissonClient;
        this.property = property;
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
        return cacheMap.computeIfAbsent(name, n -> createRedissonCache(n, ttl));
    }

    /**
     * 创建 Redisson 缓存实例
     * <p>
     * 使用 {@link RMapCache} 支持条目级原生 TTL，配置项（最大空闲时间、
     * 最大数量、驱逐模式、空值支持）复用 {@link RedissonCacheProperty}。
     * </p>
     *
     * @param name        缓存名称
     * @param businessTtl 业务 TTL，可为 null
     * @return RedissonCache 实例
     */
    private Cache createRedissonCache(String name, Duration businessTtl) {
        long ttlMillis = resolveTtlMillis(businessTtl);
        CacheConfig config = new CacheConfig(ttlMillis, property.getMaxIdleTime() * 1000);
        config.setMaxSize(property.getMaxSize());
        config.setEvictionMode(property.getEvictionMode());
        RMapCache<Object, Object> mapCache = redissonClient.getMapCache(name);
        log.info("Created redisson level cache: name={}, ttl={}ms, maxIdleTime={}s, maxSize={}",
            name, ttlMillis, property.getMaxIdleTime(), property.getMaxSize());
        return new RedissonCache(mapCache, config, property.isAllowNullValues());
    }

    /**
     * 解析生效 TTL
     * <p>
     * L2 TTL 取业务 TTL 与全局 {@code cacheTimeout} 的较小值，
     * 避免业务 TTL 超出全局配置导致缓存长期驻留。
     * </p>
     *
     * @param businessTtl 业务 TTL，可为 null
     * @return 生效 TTL（毫秒）
     */
    private long resolveTtlMillis(Duration businessTtl) {
        long defaultMillis = property.getCacheTimeout() * 1000;
        if (businessTtl == null) {
            return defaultMillis;
        }
        return Math.min(businessTtl.toMillis(), defaultMillis);
    }
}
