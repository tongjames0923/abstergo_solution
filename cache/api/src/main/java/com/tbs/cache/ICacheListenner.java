package com.tbs.cache;

import org.springframework.cache.Cache;

/**
 * 缓存监听器
 *
 * @author abstergo
 * @date 2018/05/08
 */
public interface ICacheListenner {

    /**
     * 是否支持此缓存
     *
     * @param cache
     * @return
     */
    boolean support(Cache cache);

    /**
     * 缓存获取
     *
     * @param cacheName 缓存名称
     * @param key       缓存key
     * @param value     缓存value
     */
    void onGet(String cacheName, Object key, Object value);

    /**
     * 缓存设置
     *
     * @param cacheName 缓存名称
     * @param key       缓存key
     * @param value     缓存value
     */
    void onPut(String cacheName, Object key, Object value);

    /**
     * 缓存删除
     *
     * @param cacheName 缓存名称
     * @param key       缓存key
     */
    void onEvict(String cacheName, Object key);

    /**
     * 缓存清除
     *
     * @param cacheName 缓存名称
     */
    void onClear(String cacheName);

}
