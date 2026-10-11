package com.tbs.cache.manual.impl;

import com.tbs.cache.manual.IManualCacheHandler;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapCacheHandler implements IManualCacheHandler {

    /**
     * 底层存储采用 ConcurrentHashMap，读操作天然线程安全，
     * 使 {@code CacheUtils} 的读命中路径无需再依赖外部互斥锁
     */
    private final Map<String, Object> cache = new ConcurrentHashMap<>(64);

    @Override
    public void put(String key, Object value) {
        cache.put(key, value);
    }

    @Override
    public void remove(String key) {
        cache.remove(key);
    }

    @Override
    public Object get(String key) {
        return cache.get(key);
    }

    @Override
    public boolean contains(String key) {
        return cache.containsKey(key);
    }

    @Override
    public Collection<String> keys() {
        return List.copyOf(cache.keySet());
    }
}
