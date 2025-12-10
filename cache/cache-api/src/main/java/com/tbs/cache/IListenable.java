package com.tbs.cache;

import com.tbs.common.data.ValueWrapper;
import org.springframework.cache.Cache;

/**
 * 缓存可监听接口
 * <p>
 * 定义了缓存操作事件的回调方法
 * 实现此接口可以监听缓存的获取、设置、删除和清空操作
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public interface IListenable {
    /**
     * 缓存获取事件回调
     * <p>
     * 当从缓存中获取值时触发此方法
     * </p>
     *
     * @param cache        缓存实例
     * @param key          缓存键
     * @param valueWrapper 缓存值包装器
     */
    void onGet(Cache cache, Object key, Cache.ValueWrapper valueWrapper);

    /**
     * 缓存设置事件回调
     * <p>
     * 当向缓存中设置值时触发此方法
     * </p>
     *
     * @param cache   缓存实例
     * @param key     缓存键
     * @param wrapper 缓存值包装器
     */
    void onPut(Cache cache, Object key, ValueWrapper wrapper);

    /**
     * 缓存删除事件回调
     * <p>
     * 当从缓存中删除值时触发此方法
     * </p>
     *
     * @param cache 缓存实例
     * @param key   缓存键
     */
    void onEvict(Cache cache, Object key);

    /**
     * 缓存清空事件回调
     * <p>
     * 当清除缓存中的所有值时触发此方法
     * </p>
     *
     * @param cache 缓存实例
     */
    void onClear(Cache cache);

}
