package com.tbs.cache;

import org.springframework.cache.Cache;

import java.util.Date;

import static com.tbs.cache.integration.AbstractTtlAdaptCacheMananger.CACHE_NAME_PREFIX;

/**
 * 缓存监听器接口
 * <p>
 * 用于监听缓存的各种操作，包括获取、设置、删除和清除 实现此接口可以在缓存操作发生时执行自定义逻辑
 * </p>
 *
 * @author abstergo
 * @date 2018/05/08
 * @since 1.0.0
 */
public interface ICacheListenner {

    /**
     * 判断是否支持指定的缓存
     * <p>
     * 用于确定当前监听器是否应该处理指定的缓存实例
     * </p>
     *
     * @param cache 要检查的缓存实例
     * @return 如果支持则返回true，否则返回false
     */
    boolean support(Cache cache);

    default String unwarpCacheName(String cacheName) {
        if (cacheName.startsWith(CACHE_NAME_PREFIX)) {
            return cacheName.substring(CACHE_NAME_PREFIX.length());
        }
        return cacheName;
    }

    /**
     * 缓存获取事件回调
     * <p>
     * 当从缓存中获取值时触发此方法
     * </p>
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @param value     缓存值
     */
    void onGet(String cacheName, Object key, Object value);

    /**
     * 缓存设置事件回调
     * <p>
     * 当向缓存中设置值时触发此方法
     * </p>
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @param value     缓存值
     */
    void onPut(String cacheName, Object key, Object value);

    /**
     * 缓存删除事件回调
     * <p>
     * 当从缓存中删除值时触发此方法。注意触发时机为**真正删除之前**， 此时回调方回读该缓存仍会拿到旧值。
     * </p>
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     */
    void onEvict(String cacheName, Object key);

    /**
     * 缓存删除**完成后**回调
     * <p>
     * 与 {@link #onEvict(String, Object)} 的区别只在时机：本方法在缓存值已被真正清除之后触发， 回调方在此回读得到的是「已不存在」的状态。需要「失效完成后再做后续动作」的逻辑
     * （如关联缓存级联失效、变更事件通知）必须挂在本方法，挂在 {@code onEvict} 上会读到旧值。
     * </p>
     * <p>
     * 默认空实现，保证既有监听器无需修改。
     * </p>
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     */
    default void onEvicted(String cacheName, Object key) {
    }

    /**
     * 缓存清空事件回调
     * <p>
     * 当清除缓存中的所有值时触发此方法，触发时机为**真正清空之前**。
     * </p>
     *
     * @param cacheName 缓存名称
     */
    void onClear(String cacheName);

    /**
     * 缓存清空**完成后**回调
     * <p>
     * 语义与 {@link #onEvicted(String, Object)} 一致，仅作用域为整个缓存。默认空实现。
     * </p>
     *
     * @param cacheName 缓存名称
     */
    default void onCleared(String cacheName) {
    }

    /**
     * 缓存过期事件回调
     * <p>
     * 当缓存项过期时触发此方法
     * </p>
     *
     * @param cacheName  缓存名称
     * @param key        缓存键
     * @param expireTime 过期时间
     */
    void calledExpire(String cacheName, Object key, Date expireTime);

}
