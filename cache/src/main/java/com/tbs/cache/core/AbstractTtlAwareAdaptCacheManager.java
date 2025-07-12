package com.tbs.cache.core;

import com.tbs.common.util.AssertUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * 抽象TTL缓存管理器
 *
 * @author tongj
 */
public abstract class AbstractTtlAwareAdaptCacheManager implements CacheManager {

    @Data
    @AllArgsConstructor
    public static final class MethodExpireInfo {
        private String cacheName;
        private Object key;
        private long expireTimeMillis;
        private boolean expireAll = false;

    }

    public static final class AdaptCache implements Cache {
        private Cache cache;

        public AdaptCache(Cache cache) {
            this.cache = cache;
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
            return cache.get(key);
        }

        @Override
        public <T> T get(Object key, Class<T> type) {
            return cache.get(key, type);
        }

        @Override
        public <T> T get(Object key, Callable<T> valueLoader) {
            return cache.get(key, valueLoader);
        }

        @Override
        public void put(Object key, Object value) {
            cache.put(key, value);
        }

        @Override
        public void evict(Object key) {
            cache.evict(key);
        }

        @Override
        public void clear() {
            cache.clear();
        }
    }

    /**
     * 获取缓存管理器
     *
     * @return
     */
    protected abstract CacheManager getCacheManager();

    /**
     * 获取缓存过期执行器
     *
     * @return
     */
    protected abstract IExpiredActuator getExpiredActuator();

    @Override
    public Cache getCache(String name) {
        return new AdaptCache(getCacheManager().getCache(name));
    }

    @Override
    public Collection<String> getCacheNames() {
        return getCacheManager().getCacheNames();
    }

    /**
     * 设置方法缓存过期时间
     *
     * @param methodKeyName 方法缓存key
     * @param duration      缓存过期时间
     */
    public void setMethodExpireDate(@NotNull MethodExpireInfo info) {
        AssertUtil.notNull(info.getKey(), "methodKeyName can not be null");
        if (info.isExpireAll()) {
            getExpiredActuator().expireAll(getCache(info.getCacheName()), info.getExpireTimeMillis());
        } else {
            getExpiredActuator().expire(getCache(info.getCacheName()), info.getKey(), info.getExpireTimeMillis());
        }
    }

}
