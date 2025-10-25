package com.tbs.cache;

import com.tbs.common.data.ValueWrapper;
import org.springframework.cache.Cache;

/**
 * 缓存监听器接口
 *
 * @author tongj
 */
public interface IListenable {
    /**
     * 缓存获取事件
     */
    void onGet(Cache cache, Object key, Cache.ValueWrapper valueWrapper);

    /**
     * 缓存设置事件
     */
    void onPut(Cache cache, Object key, ValueWrapper wrapper);

    /**
     * 缓存删除事件
     */
    void onEvict(Cache cache, Object key);

    /**
     * 缓存清空事件
     */
    void onClear(Cache cache);

}
