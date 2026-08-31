package com.tbs.cache.expire.impl;

import com.tbs.cache.expire.IExpireDetailsManager;
import com.tbs.cache.expire.vo.ExpireDetails;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
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
            log.debug("[ExpireDetails] 添加 nullKey 过期详情: cacheName={}, expireTime={}, isAll={}", cacheName,
                expireDetails.getExpireTime(), expireDetails.isAll());
            nullKeyExpireDetailsMap.put(cacheName, expireDetails);
            return;
        }
        log.debug("[ExpireDetails] 添加过期详情: cacheName={}, key={}, expireTime={}, isAll={}", cacheName,
            expireDetails.getKey(), expireDetails.getExpireTime(), expireDetails.isAll());
        expireDetailsMap.computeIfAbsent(cacheName, k -> {
            log.debug("[ExpireDetails] 为 cacheName={} 创建新的内部 Map", cacheName);
            return new ConcurrentHashMap<>(32);
        }).put(expireDetails.getKey(), expireDetails);
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
            ExpireDetails removed = nullKeyExpireDetailsMap.remove(cacheName);
            log.debug("[ExpireDetails] 移除 nullKey 过期详情: cacheName={}, found={}", cacheName, removed != null);
        } else {
            expireDetailsMap.computeIfPresent(cacheName, (k, v) -> {
                ExpireDetails removed = v.remove(key);
                log.debug("[ExpireDetails] 移除过期详情: cacheName={}, key={}, found={}, remainingSize={}", cacheName,
                    key, removed != null, v.size());
                if (v.isEmpty()) {
                    log.debug("[ExpireDetails] cacheName={} 内部 Map 已清空, 移除该条目", cacheName);
                    return null;
                }
                return v;
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
            ExpireDetails details = nullKeyExpireDetailsMap.get(cacheName);
            log.debug("[ExpireDetails] 查询 nullKey 过期详情: cacheName={}, found={}", cacheName, details != null);
            return details;
        }
        ExpireDetails details = expireDetailsMap.getOrDefault(cacheName, Map.of()).get(key);
        log.debug("[ExpireDetails] 查询过期详情: cacheName={}, key={}, found={}", cacheName, key, details != null);
        return details;
    }
}
