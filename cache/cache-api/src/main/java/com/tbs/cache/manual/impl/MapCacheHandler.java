package com.tbs.cache.manual.impl;

import com.tbs.cache.manual.IManualCacheHandler;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapCacheHandler implements IManualCacheHandler {

    private Map<String, Object> cache = new HashMap<>(64);

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
