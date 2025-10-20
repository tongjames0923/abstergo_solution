package com.tbs.cache;

import com.tbs.cache.limit.ICacheLimiter;
import com.tbs.cache.limit.ICacheOperationRecorder;
import com.tbs.cache.manual.IManualCacheHandler;
import com.tbs.common.data.ValueWrapper;
import com.tbs.common.function.SSupplier;
import com.tbs.common.util.helper.FeatureWrapperHelper;
import com.tbs.lock.util.LockUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class CacheUtils {
    @Resource
    ICacheLimiter limiter;

    @Resource
    ICacheOperationRecorder recorder;

    @Resource
    IManualCacheHandler manualCacheHandler;

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

}
