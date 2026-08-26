package com.tbs.cache.integration;

import com.tbs.cache.policy.ICacheTtlPolicy;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.List;

/**
 * 多层 TTL 缓存管理器抽象基类
 * <p>
 * 继承 {@link AbstractTtlAdaptCacheMananger} 获得原生 TTL 适配能力 （SYSTEM:CACHE: 前缀、装饰层兼容），内部组合 {@link MultiLevelCacheManager}
 * 作为原始缓存管理器，实现多层缓存的创建与编排。
 * </p>
 * <p>
 * 职责划分：
 * <ul>
 *   <li>{@link MultiLevelCacheManager}（组合）：多层编排，创建时绑定 per-cacheName TTL</li>
 *   <li>本类（继承）：TTL 装饰，valueTransform、监听器、@ExpireOn 兼容</li>
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
 * {@link AbstractAdaptTtlCache} 子类（可复用 {@code ITtlCacheDecorator} 链）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public abstract class AbstractMultiLevelTtlCacheMananger extends AbstractTtlAdaptCacheMananger {

    /**
     * 组合的多层缓存管理器
     */
    private final AbstractMultiLevelTtlCacheMananger delegate;

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
        this.delegate = this;
    }

    /**
     * 获取原始缓存管理器
     * <p>
     * 返回内部组合的 {@link MultiLevelCacheManager}， {@code loadCaches()} 与 {@code createCacheOnMissCache()} 均委托给它。
     * </p>
     *
     * @return 多层缓存管理器
     */
    @Override
    public CacheManager getOriginal() {
        return delegate;
    }

    /**
     * 设置原始缓存管理器
     * <p>
     * 多层缓存管理器在构造时固定组合，不支持外部替换。
     * </p>
     *
     * @param orginal 原始缓存管理器（忽略）
     */
    @Override
    public void setOriginal(CacheManager orginal) {
        // no-op：组合关系在构造时固定
    }

}
