package com.tbs.common.holder;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

/**
 * Map实现字典
 *
 * @author abstergo
 */
public abstract class AbstractMapImplDictionary<K, V> implements IDictionary<K, V> {

    /**
     * 获取Map
     *
     * @return
     */
    protected abstract Map<K, V> getMap();

    @Override
    public V get(K key) {
        return getMap().get(key);
    }

    @Override
    public void set(K key, V value) {
        getMap().put(key, value);
    }

    @Override
    public void remove(K key) {
        getMap().remove(key);
    }

    @Override
    public void clear() {
        getMap().clear();
    }

    @Override
    public boolean isEmpty() {
        return getMap().isEmpty();
    }

    @Override
    public int size() {
        return getMap().size();
    }

    @Override
    public boolean containsKey(K key) {
        return getMap().containsKey(key);
    }

    @Override
    public V computeIfAbsent(K key, Function<K, V> function) {
        return getMap().computeIfAbsent(key, function);
    }

    @Override
    public void putIfAbsent(K key, V value) {
        getMap().putIfAbsent(key, value);
    }

    @Override
    public Collection<Map.Entry<K, V>> entries() {
        return getMap().entrySet();
    }

    @Override
    public Collection<K> keys() {
        return getMap().keySet();
    }

    @Override
    public Collection<V> values() {
        return getMap().values();
    }
}
