package com.tbs.cache.impl;

import com.tbs.cache.ITtlCacheDecorator;
import com.tbs.cache.integration.AbstractMultiLevelCache;
import com.tbs.cache.integration.AbstractTtlCache;
import com.tbs.common.util.framework.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

@Slf4j
public class MultiLevelTtlCacheDecorator implements ITtlCacheDecorator {
    /**
     * 判断当前缓存是否支持TTL功能
     * <p>
     * 由于Spring Cache的限制，无法直接在Cache接口层面添加TTL功能 因此需要通过Cache的实现类或者CacheManager的实现类进行判断
     * </p>
     *
     * @param cache   要判断的缓存实例
     * @param manager 缓存管理器
     * @return 如果当前缓存支持TTL功能则返回true，否则返回false
     */
    @Override
    public boolean support(Cache cache, CacheManager manager) {
        return cache != null && cache instanceof AbstractMultiLevelCache;
    }

    private Collection<ITtlCacheDecorator> decorators = null;

    private Collection<ITtlCacheDecorator> getDecorators() {
        if (decorators == null) {
            synchronized (this) {
                if (decorators == null) {
                    decorators =
                        SpringUtil.getBeansOfType(ITtlCacheDecorator.class).values().stream().filter((t) -> t != this)
                            .sorted(Comparator.comparingInt(ITtlCacheDecorator::getOrder)).collect(Collectors.toList());
                }
            }
        }
        return decorators;
    }

    /**
     * 将普通缓存装饰成TTL缓存
     * <p>
     * 接收一个普通的Spring Cache实例和CacheManager，返回一个装饰后的AbstractTtlCache实例
     * </p>
     *
     * @param cache   要装饰的普通缓存实例
     * @param manager 缓存管理器
     * @return 装饰后的TTL缓存实例
     */
    @Override
    public AbstractTtlCache decorateCache(Cache cache, CacheManager manager) {
        AbstractMultiLevelCache c = (AbstractMultiLevelCache)cache;
        return new MultiLevelAdaptTtlCache(c, getDecorators(), manager);
    }

    /**
     * @return
     */
    @Override
    public int getOrder() {
        return 1000;
    }
}
