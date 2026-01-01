package com.tbs.common.util.base;

import com.tbs.common.data.structs.KeyValuePair;

import java.util.AbstractMap;
import java.util.Map;

/**
 * 结构工具类
 *
 * @author abstergo
 */
public class StructUtils {

    /**
     * 创建一个键值对
     *
     * @param key   键
     * @param value 值
     * @return 键值对
     */
    public static final <K, V> KeyValuePair<K, V> tuple(K key, V value) {
        return new KeyValuePair<>(key, value);
    }

    /**
     * 创建一个键值对
     *
     * @param pair 键值对
     * @return 键值对
     */
    public static final <K, V> Map.Entry<K, V> tupleToMapEntry(KeyValuePair<K, V> pair) {
        return new AbstractMap.SimpleEntry<>(pair.getKey(), pair.getValue());
    }

    /**
     * 创建一个键值对
     *
     * @param entry 键值对
     * @return 键值对
     */
    public static final <K, V> KeyValuePair<K, V> mapEntryToTuple(Map.Entry<K, V> entry) {
        return new KeyValuePair<>(entry.getKey(), entry.getValue());
    }
}
