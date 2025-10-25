package com.tbs.cache.integration;

import com.tbs.common.data.IFeatureWrapper;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.AbstractCacheManager;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 抽象的TTL缓存管理器
 *
 * @author tongj
 */
public abstract class AbstractTtlAdaptCacheMananger extends AbstractCacheManager
    implements IFeatureWrapper<CacheManager> {

    @Override
    protected Collection<? extends Cache> loadCaches() {
        return getOrginal().getCacheNames().stream().map(this::getCache).collect(Collectors.toList());
    }

    /**
     * 创建缓存
     *
     * @param name
     * @return
     */
    protected Cache createCacheOnMissCache(String name) {
        return getOrginal().getCache(name);
    }

    /**
     * 创建缓存
     *
     * @param name
     * @return
     */
    protected abstract AbstractTtlCache decorateCacheToTtl(Cache cache);

    @Override
    protected Cache getMissingCache(String name) {
        return createCacheOnMissCache(name);
    }

    @Override
    protected Cache decorateCache(Cache cache) {
        return decorateCacheToTtl(cache);
    }

}
