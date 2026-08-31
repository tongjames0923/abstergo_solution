package com.tbs.cache.caffeine.level;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.tbs.cache.caffeine.config.property.CaffeineLevelCacheProperty;
import com.tbs.cache.integration.ILevelCacheProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Caffeine L1 层级缓存提供者
 * <p>
 * 每个 cacheName 创建独立的 Caffeine 实例，支持 per-cacheName TTL 绑定。 L1 TTL = min(全局 expireAfterWrite, 业务 TTL)，确保 L1 不超过 L2 存活，
 * 保证最终一致性。
 * </p>
 * <p>
 * 层级顺序为 0（最靠近应用层），读取时优先命中。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
public class CaffeineLevelCacheProvider implements ILevelCacheProvider {

    /**
     * Caffeine 配置属性
     */
    private final CaffeineLevelCacheProperty property;

    /**
     * 已创建的缓存实例（按 cacheName 缓存）
     */
    private final ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<>(16);

    /**
     * 构造函数
     *
     * @param property Caffeine 配置属性
     */
    public CaffeineLevelCacheProvider(CaffeineLevelCacheProperty property) {
        this.property = property;
    }

    /**
     * 获取层级顺序
     * <p>
     * 进程内缓存为最快层，返回 0。
     * </p>
     *
     * @return 层级顺序 0
     */
    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * 获取或创建缓存实例（使用全局默认 TTL）
     *
     * @param name 缓存名称
     * @return 缓存实例
     */
    @Override
    public Cache getCache(String name) {
        return cacheMap.computeIfAbsent(name, n -> createCaffeineCache(n, null));
    }

    /**
     * 获取或创建带 TTL 的缓存实例
     *
     * @param name 缓存名称
     * @param ttl  业务 TTL，可为 null（使用全局默认）
     * @return 缓存实例
     */
    @Override
    public Cache getCache(String name, Duration ttl) {
        return cacheMap.computeIfAbsent(name, n -> createCaffeineCache(n, ttl));
    }

    /**
     * 创建 Caffeine 缓存实例
     * <p>
     * 有效 TTL = 优先取业务 TTL，如果业务 TTL 为 null 则取全局值。 全局值作为安全网上限，确保 L1 不会比 L2 存活更久。
     * </p>
     *
     * @param name        缓存名称
     * @param businessTtl 业务 TTL，可为 null
     * @return Caffeine 缓存实例
     */
    private Cache createCaffeineCache(String name, Duration businessTtl) {
        Duration effectiveTtl = resolveEffectiveTtl(businessTtl);

        Caffeine<Object, Object> builder =
            Caffeine.newBuilder().maximumSize(property.getMaximumSize()).expireAfterWrite(effectiveTtl);

        if (property.isRecordStats()) {
            log.debug("recordStats is true, will record cache stats");
            builder.recordStats();
        }

        com.github.benmanes.caffeine.cache.Cache<Object, Object> caffeineCache = builder.build();
        log.info(
            "Created Caffeine L1 cache: name={}, effectiveTtl={}, maximumSize={}, recordStats={}, allowNullValues={}",
            name, effectiveTtl, property.getMaximumSize(), property.isRecordStats(), property.isAllowNullValues());
        return new CaffeineCache(name, caffeineCache, property.isAllowNullValues());
    }

    /**
     * 计算有效 TTL
     * <p>
     * 优先取业务 TTL 业务 TTL 为 null 时直接使用全局值。
     * </p>
     *
     * @param businessTtl 业务 TTL，可为 null
     * @return 有效 TTL
     */
    private Duration resolveEffectiveTtl(Duration businessTtl) {
        if (businessTtl == null) {
            return property.getExpireAfterWrite();
        }
        return businessTtl;
    }
}
