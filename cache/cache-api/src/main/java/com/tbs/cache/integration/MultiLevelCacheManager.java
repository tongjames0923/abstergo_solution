package com.tbs.cache.integration;

import com.tbs.cache.policy.ICacheTtlPolicy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 多层缓存管理器
 * <p>
 * 普通的 {@link CacheManager} 实现，管理 {@link ILevelCacheProvider} 列表，
 * 为每个 cacheName 创建 {@link DefaultMultiLevelCache} 实例。
 * </p>
 * <p>
 * TTL 通过 {@link ICacheTtlPolicy} 在创建时绑定到各层引擎原生能力
 * （Caffeine expireAfterWrite / Redisson CacheConfig），遵循"谁写入，谁设 TTL"的原子写入原则。
 * </p>
 * <p>
 * 本类不提供 TTL 装饰能力，如需 valueTransform、监听器、@ExpireOn 等能力，
 * 由外层 {@link AbstractTtlAdaptCacheMananger} 装饰链包装本类实例。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
public class MultiLevelCacheManager implements CacheManager {

    /**
     * 层级提供者列表（按 order 升序排列）
     */
    private final List<ILevelCacheProvider> providers;

    /**
     * TTL 策略（可为 null，表示不启用 per-cacheName TTL）
     */
    private final ICacheTtlPolicy ttlPolicy;

    /**
     * 已创建的缓存实例
     */
    private final ConcurrentMap<String, Cache> caches = new ConcurrentHashMap<>(16);

    /**
     * 构造函数
     *
     * @param providers 层级提供者列表
     */
    public MultiLevelCacheManager(List<ILevelCacheProvider> providers) {
        this(providers, null);
    }

    /**
     * 构造函数
     *
     * @param providers 层级提供者列表
     * @param ttlPolicy TTL 策略，可为 null
     */
    public MultiLevelCacheManager(List<ILevelCacheProvider> providers, ICacheTtlPolicy ttlPolicy) {
        if (providers == null || providers.isEmpty()) {
            throw new IllegalArgumentException("At least one level cache provider is required");
        }
        List<ILevelCacheProvider> sorted = new ArrayList<>(providers);
        sorted.sort(Comparator.comparingInt(ILevelCacheProvider::getOrder));
        this.providers = Collections.unmodifiableList(sorted);
        this.ttlPolicy = ttlPolicy;
        log.info("MultiLevelCacheManager initialized: providers={}, ttlPolicy={}",
            providers.stream().map(p -> p.getClass().getSimpleName()).toList(),
            ttlPolicy != null ? ttlPolicy.getClass().getSimpleName() : "absent");
    }

    @Override
    public Cache getCache(String name) {
        return caches.computeIfAbsent(name, this::createMultiLevelCache);
    }

    @Override
    public Collection<String> getCacheNames() {
        return Collections.unmodifiableSet(caches.keySet());
    }

    /**
     * 创建多层缓存实例
     * <p>
     * 查询 TTL 策略获取业务 TTL，传递给各层提供者在创建时绑定原生 TTL。
     * </p>
     *
     * @param name 缓存名称
     * @return 多层缓存实例
     */
    protected Cache createMultiLevelCache(String name) {
        Duration businessTtl = resolveBusinessTtl(name);
        List<Cache> levels = new ArrayList<>();
        for (ILevelCacheProvider provider : providers) {
            if (provider.support(name)) {
                levels.add(provider.getCache(name, businessTtl));
            }
        }
        if (levels.isEmpty()) {
            throw new IllegalStateException("No level cache provider supports cacheName: " + name);
        }
        log.info("Created multi-level cache: name={}, levels={}, ttl={}", name, levels.size(), businessTtl);
        return new DefaultMultiLevelCache(name, levels);
    }

    /**
     * 解析业务 TTL
     *
     * @param name 缓存名称（可能带管理前缀）
     * @return 业务 TTL，可为 null
     */
    protected Duration resolveBusinessTtl(String name) {
        if (ttlPolicy == null) {
            return null;
        }
        return ttlPolicy.ttlOf(stripCacheNamePrefix(name));
    }

    /**
     * 剥离缓存名管理前缀
     *
     * @param name 缓存名称
     * @return 剥离前缀后的业务 cacheName
     */
    protected String stripCacheNamePrefix(String name) {
        if (name != null && name.startsWith(AbstractTtlAdaptCacheMananger.CACHE_NAME_PREFIX)) {
            return name.substring(AbstractTtlAdaptCacheMananger.CACHE_NAME_PREFIX.length());
        }
        return name;
    }
}
