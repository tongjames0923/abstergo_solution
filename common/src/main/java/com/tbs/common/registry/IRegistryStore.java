package com.tbs.common.registry;

import java.util.Collection;
import java.util.Set;

/**
 * 注册表存储扩展位（SPI）
 * <p>
 * 定义「键 → 一组值」这一最通用注册结构的存储契约，是 {@link KeyedRegistry} 与具体存放方式之间的唯一 seam：
 * 默认提供进程内实现 {@link InMemoryRegistryStore}，如需将注册数据落到外部介质（DB/缓存等），
 * 仅需另写一个本接口实现注入 {@link KeyedRegistry}，主流程无感。
 * </p>
 * <p>
 * 语义约定：同一键下允许挂载多个值（多播结构），单值语义由上层 {@link KeyedRegistry} 的唯一性约束保证，
 * 本接口不做唯一性判断；所有读取方法在无数据时返回空集合而非 {@code null}。
 * </p>
 *
 * @param <K> 路由键类型
 * @param <V> 注册值类型
 * @author abstergo
 * @since 1.0.0
 */
public interface IRegistryStore<K, V> {

    /**
     * 在指定键下追加一个值
     *
     * @param key   路由键，不能为null
     * @param value 注册值，不能为null
     */
    void add(K key, V value);

    /**
     * 从指定键下移除一个值，键下清空后由实现决定是否需要回收该键
     *
     * @param key   路由键，不能为null
     * @param value 待移除的注册值，不能为null
     */
    void remove(K key, V value);

    /**
     * 移除指定键及其下的全部值
     *
     * @param key 路由键，不能为null
     */
    void removeAll(K key);

    /**
     * 回查指定键下的全部值
     *
     * @param key 路由键，允许为null（视作未命中）
     * @return 该键下的值集合（只读语义），未注册时返回空集合
     */
    Collection<V> get(K key);

    /**
     * 判断指定键是否已注册
     *
     * @param key 路由键，允许为null
     * @return true 表示该键下至少有一个值
     */
    boolean containsKey(K key);

    /**
     * 列出全部已注册的键
     *
     * @return 键集合（快照语义），无注册时返回空集合
     */
    Set<K> keys();

    /**
     * 列出全部键下挂载的值（跨键扁平化）
     *
     * @return 值集合（快照语义），无注册时返回空集合
     */
    Collection<V> values();

    /**
     * 清空全部注册数据
     */
    void clear();
}
