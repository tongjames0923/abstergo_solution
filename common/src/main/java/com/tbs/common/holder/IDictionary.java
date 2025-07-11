package com.tbs.common.holder;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

/**
 * 自定义字典容器接口
 *
 * @author abstergo
 */
public interface IDictionary<K, V> {

    /**
     * 获取字典项
     *
     * @param key
     * @return
     */
    V get(K key);

    /**
     * 设置字典项
     *
     * @param key
     * @param value
     */
    void set(K key, V value);

    /**
     * 删除字典项
     *
     * @param key
     */
    void remove(K key);

    /**
     * 清空字典
     */
    void clear();

    /**
     * 字典是否为空
     *
     * @return
     */
    boolean isEmpty();

    /**
     * 获取字典大小
     *
     * @return
     */
    int size();

    /**
     * 字典是否包含key
     *
     * @param key
     * @return
     */
    boolean containsKey(K key);

    /**
     * 如果key不存在则执行function
     *
     * @param key
     * @param function
     * @return
     */
    V computeIfAbsent(K key, Function<K, V> function);

    /**
     * 如果key不存在则添加
     *
     * @param key
     * @param value
     */
    void putIfAbsent(K key, V value);

    /**
     * 获取所有键值对
     *
     * @return
     */
    Collection<Map.Entry<K, V>> entries();

    /**
     * 获取所有键
     *
     * @return
     */
    Collection<K> keys();

    /**
     * 获取所有值
     *
     * @return
     */
    Collection<V> values();
}
