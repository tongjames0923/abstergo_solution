package com.tbs.cache.core.manager;

import com.tbs.cache.core.ICacheListenner;
import com.tbs.cache.core.expire.ICacheExpiredActuator;
import com.tbs.common.util.CollUtil;
import org.springframework.cache.Cache;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 缓存适配器
 *
 * @author tongj
 */
public class TtlAdaptCache implements Cache {
    private Cache cache;
    private List<ICacheListenner> cacheListennerList;
    private ICacheExpiredActuator expiredActuator;

    private static Map<Object, Date> expireKeyMap = new ConcurrentHashMap<>(64);

    public void expireAll(Date expireTime) {
        expiredActuator.expireAll(cache, expireTime);
    }

    public String makeExpireKey(Object key) {
        return String.format("%s::%s", this.getName(), key.toString());
    }

    private void listen(Consumer<ICacheListenner> cacheListennerConsumer) {
        if (CollUtil.isEmpty(cacheListennerList)) {
            return;
        }
        cacheListennerList.forEach(cacheListennerConsumer);
    }

    /**
     * 设置缓存的过期时间
     *
     * @param key
     * @param ttl
     * @param unit
     */
    public void setTTL(Object key, Date d) {
        String k = makeExpireKey(key);
        expireKeyMap.put(k, d);
    }

    /**
     * 获取缓存的过期时间
     *
     * @param key
     * @return
     */
    public Optional<Date> getExpireTime(Object key) {
        String k = makeExpireKey(key);
        return Optional.ofNullable(expireKeyMap.get(k));
    }

    public TtlAdaptCache(Cache cache, ICacheExpiredActuator actuator, List<ICacheListenner> cacheListennerList) {
        this.cache = cache;
        this.cacheListennerList = cacheListennerList;
        this.expiredActuator = actuator;
    }

    @Override
    public String getName() {
        return cache.getName();
    }

    @Override
    public Object getNativeCache() {
        return cache.getNativeCache();
    }

    @Override
    public ValueWrapper get(Object key) {
        ValueWrapper valueWrapper = cache.get(key);
        listen(
            cacheListenner -> cacheListenner.onGet(getName(), key, valueWrapper == null ? null : valueWrapper.get()));
        return valueWrapper;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        T val = cache.get(key, type);
        listen(cacheListenner -> cacheListenner.onGet(getName(), key, val));
        return val;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        T val = cache.get(key, valueLoader);
        listen(cacheListenner -> cacheListenner.onPut(getName(), key, val));
        return val;
    }

    @Override
    public void put(Object key, Object value) {
        listen(cacheListenner -> cacheListenner.onPut(getName(), key, value));
        cache.put(key, value);
        getExpireTime(key).ifPresent(expireTime -> {
            expiredActuator.expire(cache, key, expireTime);
            expireKeyMap.remove(makeExpireKey(key));
        });
    }

    @Override
    public void evict(Object key) {
        listen(cacheListenner -> cacheListenner.onEvict(getName(), key));
        cache.evict(key);
    }

    @Override
    public void clear() {
        listen(cacheListenner -> cacheListenner.onClear(getName()));
        cache.clear();
    }
}
