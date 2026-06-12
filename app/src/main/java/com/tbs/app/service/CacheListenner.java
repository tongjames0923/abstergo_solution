package com.tbs.app.service;

import com.tbs.cache.ICacheListenner;
import com.tbs.lock.util.LockUtil;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author abstergo
 */
@Service
public class CacheListenner implements ICacheListenner {
    private final Set set;
    private Map<String, Set<Object>> keysMaps = new HashMap<>();

    public CacheListenner(Set set) {
        this.set = set;
    }

    @Override
    public boolean support(Cache cache) {
        return true;
    }

    @Override
    public void onGet(String cacheName, Object key, Object value) {
    }

    @Override
    public void onPut(String cacheName, Object key, Object value) {
        LockUtil.getInstance().lockThen(this, () -> {
            var set = keysMaps.getOrDefault(cacheName, new HashSet<>());
            set.add(key);
            keysMaps.put(cacheName, set);
        });
    }

    @Override
    public void onEvict(String cacheName, Object key) {
        LockUtil.getInstance().lockThen(this, () -> {
            var set = keysMaps.getOrDefault(cacheName, new HashSet<>());
            set.remove(key);
            keysMaps.put(cacheName, set);
        });
    }

    @Override
    public void onClear(String cacheName) {
        LockUtil.getInstance().lockThen(this, () -> {
            var set = keysMaps.getOrDefault(cacheName, new HashSet<>());
            set.clear();
            keysMaps.put(cacheName, set);
        });
    }

    public List<Object> getKeys(String cacheName) {
        return new ArrayList<>(keysMaps.getOrDefault(cacheName, new HashSet<>()));
    }

    public List<String> getCacheNames() {
        return new ArrayList<>(keysMaps.keySet());
    }

}
