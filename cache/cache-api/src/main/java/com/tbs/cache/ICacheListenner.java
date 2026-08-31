package com.tbs.cache;

import org.springframework.cache.Cache;

import java.util.Date;

/**
 * 缓存监听器接口
 * <p>
 * 用于监听缓存的各种操作，包括获取、设置、删除和清除
 * 实现此接口可以在缓存操作发生时执行自定义逻辑
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
     * 当从缓存中删除值时触发此方法
     * </p>
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     */
    void onEvict(String cacheName, Object key);

    /**
     * 缓存清除事件回调
     * <p>
     * 当清除缓存中的所有值时触发此方法
     * </p>
     *
     * @param cacheName 缓存名称
     */
    void onClear(String cacheName);

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
