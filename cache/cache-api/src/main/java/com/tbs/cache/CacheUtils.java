package com.tbs.cache;

import com.tbs.cache.limit.ICacheLimiter;
import com.tbs.cache.limit.ICacheOperationRecorder;
import com.tbs.cache.manual.IManualCacheHandler;
import com.tbs.common.IStartup;
import com.tbs.common.data.ValueWrapper;
import com.tbs.common.function.SSupplier;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.common.util.helper.FeatureWrapperHelper;
import com.tbs.lock.util.LockUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class CacheUtils implements IStartup {
    @Resource
    ICacheLimiter limiter;

    @Resource
    ICacheOperationRecorder recorder;

    @Resource
    IManualCacheHandler manualCacheHandler;

    private static CacheUtils instance = null;

    @Override
    public void startUp() throws RuntimeException {
        instance = SpringUtil.getBean(CacheUtils.class);
    }

    public static CacheUtils getInstance() {
        return instance;
    }

    /**
     * 获取缓存
     *
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> ValueWrapper<T> get(String key, Class<T> clazz) {
        ValueWrapper<T> value = ValueWrapper.empty();
        LockUtil.getInstance().lockThen(this, () -> {
            var val = FeatureWrapperHelper.unwrap(manualCacheHandler.get(key), clazz);
            recorder.onGet(manualCacheHandler, key, val.isEmpty() ? 0 : 1);
            value.set(val.orElse(null));
            limiter.limitExecution(manualCacheHandler, recorder);
        });
        return value;
    }

    /**
     * 放入缓存
     *
     * @param key
     * @param value
     */
    public <T> void put(String key, T value) {
        if (value == null) {
            return;
        }
        LockUtil.getInstance().lockThen(this, () -> {
            manualCacheHandler.put(key, value);
            recorder.onPut(manualCacheHandler, key, 1);
            limiter.limitExecution(manualCacheHandler, recorder);
        });

    }

    /**
     * 获取缓存，如果缓存中不存在则调用sSupplier获取并放入缓存
     *
     * @param key
     * @param sSupplier
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> ValueWrapper<T> computeIfAbsent(String key, SSupplier<T> sSupplier, Class<T> clazz) {
        ValueWrapper<T> val = ValueWrapper.empty();
        LockUtil.getInstance().lockThen(this, () -> {
            if (manualCacheHandler.contains(key)) {
                val.set(FeatureWrapperHelper.unwrap(manualCacheHandler.get(key), clazz).get());
            } else {
                var value = sSupplier.get();
                manualCacheHandler.put(key, value);
                recorder.onPut(manualCacheHandler, key, 1);
                limiter.limitExecution(manualCacheHandler, recorder);
                val.set(value);
            }
        });
        return val;
    }

    /**
     * 删除缓存
     *
     * @param key
     */
    public void remove(String key) {
        LockUtil.getInstance().lockThen(this, () -> {
            manualCacheHandler.remove(key);
            recorder.onRemove(manualCacheHandler, key, 1);
        });
    }

    /**
     * 将指定的值放入缓存中
     *
     * @param key   缓存的键
     * @param value 要缓存的对象值
     */
    @CachePut(cacheNames = "CACHE_UTILS_SET", key = "#key")
    public Object cacheableSet(String key, Object value) {
        return value;
    }

    /**
     * 清除指定键的缓存
     *
     * @param key 要清除的缓存键
     */
    @CacheEvict(cacheNames = "CACHE_UTILS_SET", key = "#key")
    public void cacheableEvict(String key) {
    }

    /**
     * 清除所有缓存条目
     */
    @CacheEvict(cacheNames = "CACHE_UTILS_SET", allEntries = true)
    public void cacheableEvictAll() {
    }

    /**
     * 根据键从缓存中获取值
     *
     * @param key 缓存的键
     * @return 缓存中存储的对象，如果未找到则返回null
     */
    @Cacheable(cacheNames = "CACHE_UTILS_SET", key = "#key")
    public Object cacheableGet(String key) {
        return null;
    }

}
