package com.tbs.app.service;

import com.tbs.cache.ICacheListenner;
import com.tbs.common.data.IDataHolder;
import com.tbs.common.data.IDataHolderManager;
import com.tbs.common.data.ValueWrapper;
import com.tbs.lock.util.LockUtil;
import com.tbs.web.exception.NotFoundTokenException;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author abstergo
 */
@Service
public class CacheListenner implements ICacheListenner {

    private static final String HOLDER_NAME = "CACHE_INFO_HOLDER";
    private IDataHolder holder;

    public CacheListenner(IDataHolderManager holderManager) {
        holder = holderManager.getDataHolder("CACHE_INFO_HOLDER")
            .orElseThrow(() -> new NotFoundTokenException("未获取有效的数据容器记录"));
    }

    @Override
    public boolean support(Cache cache) {
        return true;
    }

    @Override
    public void onGet(String cacheName, Object key, Object value) {
    }

    @Override
    public void onPut(String cacheName, Object key, Object value) {
        LockUtil.getInstance().lockThen(this, () -> {
            var map = holder.computeIfAbsent(HOLDER_NAME, (f) -> {
                return new HashMap();
            });
            var set = (Set)map.computeIfAbsent(cacheName, (s) -> new HashSet<>());
            set.add(key);
            map.put(cacheName, set);
            holder.set(HOLDER_NAME, map, true);

        });
    }

    @Override
    public void onEvict(String cacheName, Object key) {
        LockUtil.getInstance().lockThen(this, () -> {
            var map = holder.computeIfAbsent(HOLDER_NAME, (f) -> {
                return new HashMap();
            });
            var set = (Set)map.computeIfAbsent(cacheName, (s) -> new HashSet<>());
            set.remove(key);
            map.put(cacheName, set);
            holder.set(HOLDER_NAME, map, true);

        });
    }

    @Override
    public void onClear(String cacheName) {
        LockUtil.getInstance().lockThen(this, () -> {
            var map = holder.computeIfAbsent(HOLDER_NAME, (f) -> {
                return new HashMap();
            });
            map.remove(cacheName);
            holder.set(HOLDER_NAME, map, true);
        });
    }

    public List<Object> getKeys(String cacheName) {
        ValueWrapper<List<Object>> valueWrapper = ValueWrapper.empty();
        LockUtil.getInstance().lockThen(this, () -> {
            var map = holder.computeIfAbsent(HOLDER_NAME, (f) -> {
                return new HashMap();
            });
            Set set = (Set)map.getOrDefault(cacheName, new HashSet<>());
            List list = (List)set.stream().collect(Collectors.toList());
            valueWrapper.set(list);
        });
        return valueWrapper.get().orElse(List.of());
    }

    public List<String> getCacheNames() {
        ValueWrapper<List<String>> valueWrapper = ValueWrapper.empty();
        LockUtil.getInstance().lockThen(this, () -> {
            var map = holder.computeIfAbsent(HOLDER_NAME, (f) -> {
                return new HashMap();
            });
            var l = (List)map.keySet().stream().map((m) -> {
                return m.toString();
            }).collect(Collectors.toList());
            valueWrapper.set(l);
        });
        return valueWrapper.get().orElse(List.of());
    }

}
