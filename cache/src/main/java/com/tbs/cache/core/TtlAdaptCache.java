package com.tbs.cache.core;

import com.tbs.common.util.CollUtil;
import org.springframework.cache.Cache;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * 缓存适配器
 *
 * @author tongj
 */
public class TtlAdaptCache implements Cache {
    private Cache cache;
    private List<ICacheListenner> cacheListennerList;

    private void listen(Consumer<ICacheListenner> cacheListennerConsumer) {
        if (CollUtil.isEmpty(cacheListennerList)) {
            return;
        }
        cacheListennerList.forEach(cacheListennerConsumer);
    }

    public TtlAdaptCache(Cache cache, List<ICacheListenner> cacheListennerList) {
        this.cache = cache;
        this.cacheListennerList = cacheListennerList;
    }

    @Override
    public String getName() {
        return cache.getName();
    }

    @Override
    public Object getNativeCache() {
        return cache.getNativeCache();
    }

    @Override
    public ValueWrapper get(Object key) {
        ValueWrapper valueWrapper = cache.get(key);
        listen(
            cacheListenner -> cacheListenner.onGet(getName(), key, valueWrapper == null ? null : valueWrapper.get()));
        return valueWrapper;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        T val = cache.get(key, type);
        listen(cacheListenner -> cacheListenner.onGet(getName(), key, val));
        return val;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        T val = cache.get(key, valueLoader);
        listen(cacheListenner -> cacheListenner.onPut(getName(), key, val));
        return val;
    }

    @Override
    public void put(Object key, Object value) {
        listen(cacheListenner -> cacheListenner.onPut(getName(), key, value));
        cache.put(key, value);
    }

    @Override
    public void evict(Object key) {
        listen(cacheListenner -> cacheListenner.onEvict(getName(), key));
        cache.evict(key);
    }

    @Override
    public void clear() {
        listen(cacheListenner -> cacheListenner.onClear(getName()));
        cache.clear();
    }
}
