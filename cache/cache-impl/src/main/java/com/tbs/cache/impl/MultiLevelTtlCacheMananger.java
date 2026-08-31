package com.tbs.cache.impl;

import com.tbs.cache.ITtlCacheDecorator;
import com.tbs.cache.integration.AbstractMultiLevelTtlCacheMananger;
import com.tbs.cache.integration.AbstractTtlCache;
import com.tbs.cache.integration.ILevelCacheProvider;
import com.tbs.cache.policy.ICacheTtlPolicy;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.framework.SpringUtil;
import org.springframework.cache.Cache;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 多级 TTL 缓存管理器默认实现
 * <p>
 * 继承 {@link AbstractMultiLevelTtlCacheMananger}，通过 Spring 容器自动收集
 * 所有 {@link ILevelCacheProvider} Bean 组装层级，装饰逻辑复用
 * {@link ITtlCacheDecorator} 装饰器链（与 {@link DefaultTtlAdaptCacheMananger} 一致）。
 * </p>
 * <p>
 * 完整链路：
 * <pre>
 * AbstractTtlCache（装饰层：序列化、事件、@ExpireOn）
 *   └── DefaultMultiLevelCache（编排层：读穿透、分层写）
 *          ├── L1 CaffeineCache（CaffeineLevelCacheProvider，order=0）
 *          └── L2 RedissonCache（RedissonLevelCacheProvider，order=1）
 * </pre>
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class MultiLevelTtlCacheMananger extends AbstractMultiLevelTtlCacheMananger {

    /**
     * TTL 缓存装饰器集合（懒加载，按 order 排序）
     */
    private volatile Collection<ITtlCacheDecorator> ttlCacheDecorators;

    /**
     * 构造函数
     *
     * @param providers 层级提供者列表
     * @param ttlPolicy TTL 策略，可为 null
     */
    public MultiLevelTtlCacheMananger(List<ILevelCacheProvider> providers, ICacheTtlPolicy ttlPolicy) {
        super(providers, ttlPolicy);
    }

    /**
     * 获取 TTL 缓存装饰器集合
     * <p>
     * 从 Spring 容器获取所有 {@link ITtlCacheDecorator} Bean，按 order 升序。
     * 双重检查锁保证线程安全的懒加载。
     * </p>
     *
     * @return 排序后的装饰器集合
     */
    protected Collection<ITtlCacheDecorator> getTtlCacheDecorators() {
        if (ttlCacheDecorators == null) {
            synchronized (this) {
                if (ttlCacheDecorators == null) {
                    ttlCacheDecorators = SpringUtil.getBeansOfType(ITtlCacheDecorator.class).values().stream()
                        .sorted(Comparator.comparingInt(ITtlCacheDecorator::getOrder)).collect(Collectors.toList());
                }
            }
        }
        return ttlCacheDecorators;
    }

    /**
     * 将多层缓存装饰为 TTL 缓存
     * <p>
     * 复用 {@link ITtlCacheDecorator} 装饰器链，与 {@link DefaultTtlAdaptCacheMananger}
     * 保持一致的装饰行为（valueTransform、监听器、@ExpireOn 兼容）。
     * </p>
     *
     * @param cache 多层缓存实例
     * @return 装饰后的 TTL 缓存
     */
    @Override
    protected AbstractTtlCache decorateCacheToTtl(Cache cache) {
        if (CollUtil.isNotEmpty(this.getTtlCacheDecorators())) {
            for (ITtlCacheDecorator decorator : this.getTtlCacheDecorators()) {
                if (decorator.support(cache, getOriginal())) {
                    return decorator.decorateCache(cache, getOriginal());
                }
            }
        }
        AssertUtil.isTrue(true).errorLevel(ErrorLevelEnum.CRITICAL_SYSTEM_ERROR)
            .message("No TTL cache decorator found for cache: {}", cache.getName()).throwIt();
        return null;
    }
}
