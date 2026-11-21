package com.tbs.cache.integration;

import com.tbs.cache.policy.ICacheTtlPolicy;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.i18n.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 多层 TTL 缓存管理器抽象基类
 * <p>
 * 继承 {@link AbstractTtlAdaptCacheMananger} 获得原生 TTL 适配能力 （SYSTEM:CACHE: 前缀、装饰层兼容），
 * 自身管理 {@link ILevelCacheProvider} 列表，直接创建 {@link DefaultMultiLevelCache} 实例。
 * </p>
 * <p>
 * 职责划分：
 * <ul>
 *   <li>本类：多层编排创建（读穿透、分层写），创建时绑定 per-cacheName TTL</li>
 *   <li>装饰层（{@link #decorateCacheToTtl(Cache)}）：valueTransform、监听器、@ExpireOn 兼容</li>
 * </ul>
 * </p>
 * <p>
 * 完整链路：
 * <pre>
 * AbstractTtlCache（装饰层：序列化、事件、过期标记）
 *   └── getOriginal() → DefaultMultiLevelCache（编排层：读穿透、分层写）
 *                          ├── L1 CaffeineCache（创建时绑定 TTL）
 *                          └── L2 RedissonCache（创建时绑定 TTL）
 * </pre>
 * </p>
 * <p>
 * 子类需要实现 {@link #decorateCacheToTtl(Cache)}，将多层缓存包装为
 * {@link AbstractTtlCache} 子类（可复用 {@code ITtlCacheDecorator} 链）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractMultiLevelTtlCacheMananger extends AbstractTtlAdaptCacheMananger {

    /**
     * 层级提供者列表（按 order 升序排列，index 0 为最快层）
     */
    private final List<ILevelCacheProvider> providers;

    /**
     * TTL 策略（可为 null，表示不启用 per-cacheName TTL）
     */
    private final ICacheTtlPolicy ttlPolicy;

    /**
     * 构造函数
     *
     * @param providers 层级提供者列表
     */
    protected AbstractMultiLevelTtlCacheMananger(List<ILevelCacheProvider> providers) {
        this(providers, null);
    }

    /**
     * 构造函数
     *
     * @param providers 层级提供者列表
     * @param ttlPolicy TTL 策略，可为 null（不启用 per-cacheName TTL）
     */
    protected AbstractMultiLevelTtlCacheMananger(List<ILevelCacheProvider> providers, ICacheTtlPolicy ttlPolicy) {
        if (providers == null || providers.isEmpty()) {
            AssertUtil.throwError(() ->
                new IllegalArgumentException(I18nUtil.get("cache.level.provider.required")));
        }
        List<ILevelCacheProvider> sorted = new ArrayList<>(providers);
        sorted.sort(Comparator.comparingInt(ILevelCacheProvider::getOrder));
        this.providers = Collections.unmodifiableList(sorted);
        this.ttlPolicy = ttlPolicy;

        String providerDetails = providers.stream()
            .map(p -> p.getClass().getSimpleName() + "(order=" + p.getOrder() + ")")
            .collect(Collectors.joining(", "));

        log.info("Multi-level cache manager initialized: providerCount={}, providers=[{}], ttlPolicy={}, defaultTtl={}",
            providers.size(),
            providerDetails,
            ttlPolicy != null ? ttlPolicy.getClass().getSimpleName() : "absent",
            ttlPolicy != null ? formatDuration(ttlPolicy.defaultTtl()) : "N/A");
    }

    /**
     * 获取原始缓存管理器
     * <p>
     * 多层缓存管理器自身即创建者，返回自身以兼容 {@code IFeatureWrapper} 契约。
     * {@code loadCaches()} 与 {@code createCacheOnMissCache()} 均已覆写为自管实现。
     * </p>
     *
     * @return 自身实例
     */
    @Override
    public CacheManager getOriginal() {
        return this;
    }

    /**
     * 设置原始缓存管理器
     * <p>
     * 多层缓存管理器自管缓存创建，不支持外部替换原始管理器。
     * </p>
     *
     * @param orginal 原始缓存管理器（忽略）
     */
    @Override
    public void setOriginal(CacheManager orginal) {
        log.warn("setOriginal called on multi-level cache manager (no-op): self-managed mode, external replacement not supported. caller={}",
            orginal != null ? orginal.getClass().getSimpleName() : "null");
    }

    /**
     * 预加载缓存列表
     * <p>
     * 多层缓存按需创建，不预加载。覆写父类委托 {@code getOriginal()} 的实现， 避免自管模式下的递归。
     * </p>
     *
     * @return 空集合
     */
    @Override
    protected Collection<? extends Cache> loadCaches() {
        log.debug("loadCaches skipped: multi-level cache uses on-demand creation via createCacheOnMissCache");
        return Collections.emptyList();
    }

    /**
     * 缓存缺失时创建多层缓存
     * <p>
     * 覆写父类委托 {@code getOriginal()} 的实现，避免自管模式下的无限递归。
     * 查询 TTL 策略获取业务 TTL，传递给各层提供者在创建时绑定引擎原生 TTL。
     * </p>
     *
     * @param name 缓存名称（带 SYSTEM:CACHE: 前缀）
     * @return 多层缓存实例
     */
    @Override
    protected Cache createCacheOnMissCache(String name) {
        String businessName = name.startsWith(CACHE_NAME_PREFIX) ? name.substring(CACHE_NAME_PREFIX.length()) : name;
        Duration businessTtl = resolveBusinessTtl(name);

        log.info("Creating multi-level cache: internalName={}, businessName={}, ttl={}",
            name, businessName, formatDuration(businessTtl));

        List<Cache> levels = new ArrayList<>();
        List<String> skippedProviders = new ArrayList<>();

        for (ILevelCacheProvider provider : providers) {
            if (provider.support(name)) {
                Cache cache = provider.getCache(name, businessTtl);
                levels.add(cache);
                log.info("  L{}: provider={}, cacheClass={}, ttl={}",
                    levels.size(),
                    provider.getClass().getSimpleName(),
                    cache.getClass().getSimpleName(),
                    formatDuration(businessTtl));
            } else {
                skippedProviders.add(provider.getClass().getSimpleName());
            }
        }

        if (levels.isEmpty()) {
            log.error("No level cache provider supports cacheName: internalName={}, businessName={}, availableProviders={}",
                name, businessName,
                providers.stream().map(p -> p.getClass().getSimpleName()).collect(Collectors.joining(", ")));
            AssertUtil.throwError(() -> new IllegalStateException(
                I18nUtil.get("cache.level.provider.not.found", name)));
        }

        if (!skippedProviders.isEmpty()) {
            log.debug("Skipped providers (not supporting cacheName={}): [{}]",
                businessName, String.join(", ", skippedProviders));
        }

        log.info("Created multi-level cache: internalName={}, businessName={}, ttl={}, levelCount={}, levels=[{}]",
            name, businessName, formatDuration(businessTtl), levels.size(),
            levels.stream().map(c -> c.getClass().getSimpleName()).collect(Collectors.joining(", ")));

        return new DefaultMultiLevelCache(name, levels);
    }

    /**
     * 获取层级提供者列表
     *
     * @return 不可变的层级提供者列表（按 order 升序）
     */
    protected List<ILevelCacheProvider> getProviders() {
        return providers;
    }

    /**
     * 解析业务 TTL
     * <p>
     * TTL 策略以业务声明的原始 cacheName 为键，需先剥离 {@link #CACHE_NAME_PREFIX} 管理前缀。
     * </p>
     *
     * @param name 缓存名称（可能带管理前缀）
     * @return 业务 TTL，可为 null
     */
    protected Duration resolveBusinessTtl(String name) {
        String businessName = name.startsWith(CACHE_NAME_PREFIX) ? name.substring(CACHE_NAME_PREFIX.length()) : name;

        if (ttlPolicy == null) {
            log.debug("TTL policy absent, no business TTL for: businessName={}, internalName={}",
                businessName, name);
            return null;
        }

        var ttlO = ttlPolicy.findOverride(businessName);
        Duration ttl = null;
        if (ttlO.isPresent()) {
            ttl = ttlO.get();
            log.info("TTL policy override hit: businessName={}, overrideTtl={}, policyClass={}",
                businessName, formatDuration(ttl), ttlPolicy.getClass().getSimpleName());
        } else {
            ttl = ttlPolicy.defaultTtl();
            log.debug("TTL policy default: businessName={}, defaultTtl={}, policyClass={}",
                businessName, formatDuration(ttl), ttlPolicy.getClass().getSimpleName());
        }

        return ttl;
    }

    /**
     * 格式化 Duration 为人类可读字符串
     * <p>
     * 示例：1h、30m、45s、1h30m
     * </p>
     *
     * @param duration Duration，可为 null
     * @return 格式化后的字符串，null 返回 "null"
     */
    private String formatDuration(Duration duration) {
        if (duration == null) {
            return "null";
        }
        long seconds = duration.getSeconds();
        if (seconds < 0) {
            return "0s";
        }
        if (seconds < 60) {
            return seconds + "s";
        }
        if (seconds < 3600) {
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;
            return remainingSeconds > 0 ? minutes + "m" + remainingSeconds + "s" : minutes + "m";
        }
        long hours = seconds / 3600;
        long remainingMinutes = (seconds % 3600) / 60;
        return remainingMinutes > 0 ? hours + "h" + remainingMinutes + "m" : hours + "h";
    }
}
