package com.tbs.common.data;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 缓存键匹配器
 *
 * @author tongj
 */
public interface IMatchKeyFounder {
    /**
     * 匹配缓存键
     *
     * @param key
     * @return
     */
    default Collection<String> find(String key) {
        return findEntries(key).stream().map(Map.Entry::getKey).collect(Collectors.toList());
    }

    /**
     * 匹配缓存键对应的值
     *
     * @param key
     * @return
     */
    default Collection<Object> findValues(String key) {
        return findEntries(key).stream().map(Map.Entry::getValue).collect(Collectors.toList());
    }

    /**
     * 匹配缓存键对应的键值对
     *
     * @param key
     * @return
     */
    Collection<Map.Entry<String, Object>> findEntries(String key);
}
