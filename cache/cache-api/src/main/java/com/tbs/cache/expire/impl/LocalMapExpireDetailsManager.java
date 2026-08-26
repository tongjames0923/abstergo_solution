package com.tbs.cache.expire.impl;

import com.tbs.cache.expire.IExpireDetailsManager;
import com.tbs.cache.expire.vo.ExpireDetails;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalMapExpireDetailsManager implements IExpireDetailsManager {
    private Map<String, Map<Object, ExpireDetails>> expireDetailsMap = new ConcurrentHashMap<>(32);
    private Map<String, ExpireDetails> nullKeyExpireDetailsMap = new ConcurrentHashMap<>(32);

    /**
     * 添加缓存过期详细信息
     *
     * @param cacheName
     * @param expireDetails 缓存过期详细信息
     */
    @Override
    public void addExpireDetails(String cacheName, ExpireDetails expireDetails) {
        if (expireDetails.getKey() == null) {
            nullKeyExpireDetailsMap.put(cacheName, expireDetails);
            return;
        } else {
            expireDetailsMap.computeIfAbsent(cacheName, k -> new ConcurrentHashMap<>(32))
                .put(expireDetails.getKey(), expireDetails);
        }
    }

    /**
     * 移除缓存过期详细信息
     *
     * @param cacheName
     * @param key       缓存键
     */
    @Override
    public void getExpireDetailsAndRemove(String cacheName, Object key) {
        if (key == null) {
            nullKeyExpireDetailsMap.remove(cacheName);
        } else {
            expireDetailsMap.computeIfPresent(cacheName, (k, v) -> {
                v.remove(key);
                return v.isEmpty() ? null : v;
            });
        }
    }

    /**
     * 获取缓存过期详细信息
     *
     * @param cacheName
     * @param key       缓存键
     */
    @Override
    public ExpireDetails getExpireDetails(String cacheName, Object key) {
        if (key == null) {
            return nullKeyExpireDetailsMap.get(cacheName);
        }
        return expireDetailsMap.getOrDefault(cacheName, Map.of()).get(key);
    }
}
