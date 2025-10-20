package com.tbs.cache.integration;

import com.tbs.cache.CacheUtils;
import com.tbs.cache.ICacheListenner;
import com.tbs.cache.IListenable;
import com.tbs.cache.expire.ICacheExpiredActuator;
import com.tbs.common.data.IDataBridge;
import com.tbs.common.data.IFeatureWrapper;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.framework.SpringUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

@Slf4j
public abstract class AbstractTtlCache implements Cache, ICacheExpiredActuator, IFeatureWrapper<Cache>, IListenable {

    protected abstract List<ICacheListenner> getListeners();

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        return getOrginal().putIfAbsent(key, value);
    }

    @Override
    public boolean evictIfPresent(Object key) {
        return getOrginal().evictIfPresent(key);
    }

    @Override
    public boolean invalidate() {
        return getOrginal().invalidate();
    }

    @Override
    public String getName() {
        return getOrginal().getName();
    }

    @Override
    public Object getNativeCache() {
        return getOrginal().getNativeCache();
    }

    @Data
    @AllArgsConstructor
    private class TransformInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        private Class<?> type;
        Class<? extends IDataBridge> bridgeType;
        IDataBridge bridge;
    }

    private static final int TRANSFORM_READ = 0;
    private static final int TRANSFORM_WRITE = 1;

    private Object valueTransform(@NotNull Object value, int mode) {
        Class<?> clazz = value.getClass();
        var info = CacheUtils.getInstance().computeIfAbsent("CACHE_TRANSFORM_INFO" + clazz.getName(), () -> {
            if (!clazz.isAnnotationPresent(com.tbs.cache.serialize.annotation.CacheTransform.class)) {
                return new TransformInfo(clazz, null, null);
            }
            var cacheTransform = clazz.getDeclaredAnnotation(com.tbs.cache.serialize.annotation.CacheTransform.class);
            return new TransformInfo(clazz, cacheTransform.bridgeType(),
                StrUtil.isEmpty(cacheTransform.bridgeName()) ? SpringUtil.getBean(cacheTransform.bridgeType())
                    : SpringUtil.getBean(cacheTransform.bridgeName(), cacheTransform.bridgeType()));
        }, TransformInfo.class).get();
        if (info.isPresent()) {
            var bridge = info.get().getBridge();
            if (bridge != null) {
                if (mode == TRANSFORM_WRITE) {
                    return bridge.bridge(value);
                } else if (mode == TRANSFORM_READ) {
                    return bridge.unbridge(value);
                }
            }
        }
        return value;
    }

    @Override
    public ValueWrapper get(Object key) {
        var val = getOrginal().get(key);
        onGet(this, key, val);
        if (val != null && val.get() != null) {
            return new SimpleValueWrapper(valueTransform(val.get(), TRANSFORM_READ));
        }
        return val;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {

        var val = getOrginal().get(key, type);
        onGet(this, key, new SimpleValueWrapper(val));
        return val;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        var val = getOrginal().get(key, valueLoader);
        onGet(this, key, new SimpleValueWrapper(val));
        return val;
    }

    @Override
    public void put(Object key, Object value) {
        value = valueTransform(value, TRANSFORM_WRITE);
        var wrapper = com.tbs.common.data.ValueWrapper.of(value);
        onPut(this, key, wrapper);
        getOrginal().put(key, wrapper.get().orElse(null));
        com.tbs.common.data.ValueWrapper<Boolean> allExpired = com.tbs.common.data.ValueWrapper.of(false);
        isCallToExpireAll().ifPresent((date) -> {
            expireAll(AbstractTtlCache.this, date);
            allExpired.set(true);
        });
        if (!allExpired.get().orElse(false)) {
            isCallToExpire(key).ifPresent((date) -> {
                expire(AbstractTtlCache.this, key, date);
            });
        }
    }

    @Override
    public void evict(Object key) {
        onEvict(this, key);
        getOrginal().evict(key);
    }

    @Override
    public void clear() {
        onClear(this);
        getOrginal().clear();
    }

    /**
     * 外部调用请求缓存过期
     *
     * @param key
     * @param expireTime
     */
    public abstract void callToExpire(Object key, Date expireTime);

    /**
     * 外部调用请求缓存过期
     *
     * @param expireTime
     */
    public abstract void callToExpireAll(Date expireTime);

    /**
     * 判断是否是外部调用请求缓存过期
     *
     * @param key
     * @return
     */
    protected abstract Optional<Date> isCallToExpire(Object key);

    /**
     * 获取缓存的过期时间
     *
     * @param key
     * @return
     */
    protected abstract Optional<Date> isCallToExpireAll();

    @Override
    public void onClear(Cache cache) {
        getListeners().forEach((listener) -> {
            listener.onClear(cache.getName());
        });
    }

    @Override
    public void onEvict(Cache cache, Object key) {
        getListeners().forEach((listener) -> {
            listener.onEvict(cache.getName(), key);
        });
    }

    @Override
    public void onPut(Cache cache, Object key, com.tbs.common.data.ValueWrapper wrapper) {
        getListeners().forEach((listener) -> {
            listener.onPut(cache.getName(), key, wrapper);
        });
    }

    @Override
    public void onGet(Cache cache, Object key, ValueWrapper valueWrapper) {
        getListeners().forEach((listener) -> {
            listener.onGet(cache.getName(), key, valueWrapper);
        });
    }
}
