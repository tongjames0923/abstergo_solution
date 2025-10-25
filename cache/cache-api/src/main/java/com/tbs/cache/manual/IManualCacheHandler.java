package com.tbs.cache.manual;

import java.util.Collection;

/**
 * 手动缓存处理器
 *
 * @author abstergo
 */
public interface IManualCacheHandler {

    /**
     * 放入缓存
     *
     * @param key
     * @param value
     */
    void put(String key, Object value);

    /**
     * 删除缓存
     *
     * @param key
     */
    void remove(String key);

    /**
     * 获取缓存
     *
     * @param key
     * @return
     */
    Object get(String key);

    /**
     * 判断缓存中是否存在
     *
     * @param key
     * @return
     */
    boolean contains(String key);

    /**
     * 获取缓存中所有的key
     *
     * @return
     */
    Collection<String> keys();

}
