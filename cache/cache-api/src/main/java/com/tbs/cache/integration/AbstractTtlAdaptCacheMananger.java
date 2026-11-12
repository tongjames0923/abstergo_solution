package com.tbs.cache.integration;

import com.tbs.common.data.IFeatureWrapper;
import org.jetbrains.annotations.Nullable;
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
        return getOriginal().getCacheNames().stream().map(this::getCache).collect(Collectors.toList());
    }

    public static final String CACHE_NAME_PREFIX = "";

    @Override
    public @Nullable Cache getCache(String name) {
        if (name.startsWith(CACHE_NAME_PREFIX)) {
            return super.getCache(name);
        }
        return super.getCache(CACHE_NAME_PREFIX + name);
    }

    /**
     * 创建缓存
     *
     * @param name
     * @return
     */
    protected Cache createCacheOnMissCache(String name) {
        return getOriginal().getCache(name);
    }

    /**
     * 装饰缓存
     *
     * @param cache
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
