package com.tbs.cache.core.manager.holder;

import com.tbs.cache.config.CacheProperty;
import com.tbs.common.data.IDataHolder;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * 缓存管理器
 *
 * @author tongj
 */
public class DataHolderCacheManager implements CacheManager {

    @Resource
    CacheProperty property;

    IDataHolder dataHolder;
    private volatile Set<String> cacheNames;

    /**
     * @param dataHolder
     * @param cacheNames
     */
    public DataHolderCacheManager(IDataHolder dataHolder, Set<String> cacheNames) {
        this.dataHolder = dataHolder;
        this.cacheNames = cacheNames == null ? Collections.emptySet() : cacheNames;
    }

    @Override
    public Cache getCache(String name) {
        return new DataHolderCache(property.isAllowNullValues(), dataHolder, name);
    }

    @Override
    public Collection<String> getCacheNames() {
        return cacheNames;
    }
}
