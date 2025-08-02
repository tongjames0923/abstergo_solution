package com.tbs.common.data;

import com.tbs.common.function.SFunction;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * 数据存储接口
 *
 * @author abstergo
 */
public interface IDataHolder {
    /**
     * 存储数据
     *
     * @param key
     * @param value
     */
    boolean set(String key, Object value, boolean force);

    /**
     * 获取数据
     *
     * @param key
     * @param function
     * @param <T>
     * @return
     */
    default <T> T computeIfAbsent(String key, SFunction<String, T> function) {
        if (containsKey(key)) {
            return (T)get(key);
        } else {
            T value = function.apply(key);
            set(key, value, true);
            return value;
        }
    }

    /**
     * 获取数据
     *
     * @param key
     * @return
     */
    <T> Optional<T> get(String key);

    /**
     * 删除数据
     *
     * @param key
     */
    void remove(String key);

    /**
     * 清空数据
     */
    void clear();

    /**
     * 数据是否为空
     *
     * @return
     */
    boolean isEmpty();

    /**
     * 数据大小
     *
     * @return
     */
    int size();

    /**
     * 数据是否包含某个key
     *
     * @param key
     * @return
     */
    boolean containsKey(String key);

    /**
     * 获取所有数据
     *
     * @return
     */
    Collection<Map.Entry<String, Object>> entries();

    /**
     * 获取所有key
     *
     * @return
     */
    Collection<String> keys();

    /**
     * 获取所有value
     *
     * @return
     */
    Collection<Object> values();
}
