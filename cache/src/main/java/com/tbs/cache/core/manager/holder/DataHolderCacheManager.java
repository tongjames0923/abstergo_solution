package com.tbs.cache.core.manager.holder;

import com.tbs.cache.config.CacheProperty;
import com.tbs.common.data.IDataHolder;
import com.tbs.common.data.IDataHolderManager;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 缓存管理器
 *
 * @author tongj
 */
public class DataHolderCacheManager implements CacheManager {

    @Resource
    CacheProperty property;

    @Resource
    private IDataHolderManager dataHolderManager;

    private Map<String, Cache> cacheMap = new HashMap<>(8);


    @Override
    public Cache getCache(String name) {
        return cacheMap.computeIfAbsent(name, (k) -> {
            IDataHolder dataHolder =
                dataHolderManager.getDataHolder(name).orElseThrow(() -> new RuntimeException("未找到缓存"));
            return new DataHolderCache(property.isAllowNullValues(), dataHolder, name);
        });
    }

    @Override
    public Collection<String> getCacheNames() {
        return cacheMap.keySet();
    }
}
