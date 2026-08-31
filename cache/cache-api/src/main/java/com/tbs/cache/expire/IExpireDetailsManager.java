package com.tbs.cache.expire;

import com.tbs.cache.expire.vo.ExpireDetails;

/**
 * 缓存过期详细信息管理器
 *
 * @author tbs
 */
public interface IExpireDetailsManager {
    /**
     * 添加缓存过期详细信息
     *
     * @param key           缓存键
     * @param expireDetails 缓存过期详细信息
     */
    void addExpireDetails(String cacheName, ExpireDetails expireDetails);

    /**
     * 移除缓存过期详细信息
     *
     * @param key 缓存键
     */
    void getExpireDetailsAndRemove(String cacheName, Object key);

    /**
     * 获取缓存过期详细信息
     *
     * @param key 缓存键
     */
    ExpireDetails getExpireDetails(String cacheName, Object key);
}
