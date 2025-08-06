package com.tbs.common.data.impl;

import com.tbs.common.data.IDataHolder;
import com.tbs.common.error.AssertUtil;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Map数据存储实现
 *
 * @author abstergo
 */
public class MapDataHolder extends AbstractAntStyleMatchKeyFounder implements IDataHolder {

    private Map<String, Object> map;

    public MapDataHolder(Map<String, Object> map) {
        AssertUtil.notNull(map, "map is null");
        this.map = map;
    }

    public MapDataHolder() {
        this.map = new LinkedHashMap<>(8);
    }

    @Override
    public boolean set(String key, Object value, boolean force) {
        if (force) {
            map.put(key, value);
            return true;
        }
        if (map.containsKey(key)) {
            return false;
        }
        map.put(key, value);
        return true;
    }

    @Override
    public <T> Optional<T> get(String key) {
        if (map.containsKey(key)) {
            Object val = map.get(key);
            return Optional.ofNullable((T)val);
        }
        return Optional.empty();
    }

    @Override
    public void remove(String key) {
        map.remove(key);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    @Override
    public Collection<Map.Entry<String, Object>> entries() {
        return map.entrySet();
    }

    @Override
    public Collection<String> keys() {
        return map.keySet();
    }

    @Override
    public Collection<Object> values() {
        return map.values();
    }

    @Override
    protected Collection<Map.Entry<String, Object>> getEntries() {
        return map.entrySet();
    }
}
