package com.tbs.cache.integration;

import com.tbs.cache.ICacheListenner;
import com.tbs.cache.IListenable;
import com.tbs.cache.expire.ICacheExpiredActuator;
import com.tbs.cache.expire.IExpireDetailsManager;
import com.tbs.cache.expire.vo.ExpireDetails;
import com.tbs.common.data.IFeatureWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.tbs.cache.serialize.utils.ValueTransformUtils.*;

@Slf4j
public abstract class AbstractTtlCache implements Cache, ICacheExpiredActuator, IFeatureWrapper<Cache>, IListenable {

    /**
     * 获取监听器
     *
     * @return
     */
    protected abstract List<ICacheListenner> getListeners();

    private volatile List<ICacheListenner> listeners;

    List<ICacheListenner> getAvaliableListeners() {
        if (listeners == null) {
            synchronized (this) {
                if (listeners == null) {
                    listeners = getListeners().stream().filter((listener) -> listener.support(this))
                        .collect(Collectors.toList());
                }
            }
        }
        return listeners;
    }

    private void foreachListener(Consumer<ICacheListenner> s) {
        getAvaliableListeners().forEach((listener) -> {
            try {
                s.accept(listener);
            } catch (Exception e) {
                log.error("cache listener error", e);
            }
        });
    }

    /**
     * 获取过期时间管理器
     *
     * @return
     */
    protected abstract IExpireDetailsManager getExpireDetailsManager();

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        return getOriginal().putIfAbsent(key, value);
    }

    @Override
    public boolean evictIfPresent(Object key) {
        return getOriginal().evictIfPresent(key);
    }

    @Override
    public boolean invalidate() {
        return getOriginal().invalidate();
    }

    @Override
    public String getName() {
        return getOriginal().getName();
    }

    @Override
    public Object getNativeCache() {
        return getOriginal().getNativeCache();
    }



    @Override
    public ValueWrapper get(Object key) {
        var val = getOriginal().get(key);
        if (val != null && val.get() != null) {
            val = new SimpleValueWrapper(valueTransform(val.get(), TRANSFORM_READ));
        }
        onGet(this, key, val);
        return val;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {

        var val = getOriginal().get(key, type);
        onGet(this, key, new SimpleValueWrapper(val));
        return val;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        var val = getOriginal().get(key, valueLoader);
        onGet(this, key, new SimpleValueWrapper(val));
        return val;
    }

    @Override
    public void put(Object key, Object value) {
        if (value != null) {
            value = valueTransform(value, TRANSFORM_WRITE);
        }
        var wrapper = com.tbs.common.data.ValueWrapper.of(value);
        onPut(this, key, wrapper);
        getOriginal().put(key, wrapper.get().orElse(null));
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

    /**
     * 缓存过期处理
     *
     * @param cache
     * @param key
     * @param expireTime
     * @return
     */
    @Override
    public boolean expire(Cache cache, Object key, Date expireTime) {
        onExpire(cache, key, expireTime);
        return true;
    }

    /**
     * 缓存全部过期处理
     *
     * @param cache
     * @param expireTime
     * @return
     */
    @Override
    public boolean expireAll(Cache cache, Date expireTime) {
        onExpire(cache, null, expireTime);
        return true;
    }

    @Override
    public void evict(Object key) {
        onEvict(this, key);
        getOriginal().evict(key);
    }

    @Override
    public void clear() {
        onClear(this);
        getOriginal().clear();
    }

    /**
     * 外部调用请求缓存过期
     *
     * @param key
     * @param expireTime
     */
    public void callToExpire(Object key, Date expireTime) {
        getExpireDetailsManager().addExpireDetails(this.getName(), new ExpireDetails(expireTime, false, key));
    }

    /**
     * 外部调用请求缓存过期
     *
     * @param expireTime
     */
    public void callToExpireAll(Date expireTime) {
        getExpireDetailsManager().addExpireDetails(this.getName(), new ExpireDetails(expireTime, true, null));
    }

    /**
     * 判断是否是外部调用请求缓存过期
     *
     * @param key
     * @return
     */
    protected Optional<Date> isCallToExpire(Object key) {
        var expireDetails = Optional.ofNullable(getExpireDetailsManager().getExpireDetails(this.getName(), key));

        if (expireDetails.isPresent()) {
            return Optional.ofNullable(expireDetails.get().getExpireTime());
        }
        return Optional.empty();
    }

    /**
     * 获取缓存的过期时间
     *
     * @param key
     * @return
     */
    protected Optional<Date> isCallToExpireAll() {
        return Optional.ofNullable(getExpireDetailsManager().getExpireDetails(this.getName(), null))
            .map(ExpireDetails::getExpireTime);
    }

    @Override
    public void onClear(Cache cache) {
        this.foreachListener((listener) -> {
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
        this.foreachListener((listener) -> {
            listener.onPut(cache.getName(), key, wrapper);
        });
    }

    @Override
    public void onGet(Cache cache, Object key, ValueWrapper valueWrapper) {
        this.foreachListener((listener) -> {
            listener.onGet(cache.getName(), key, valueWrapper);
        });
    }

    /**
     * 缓存过期事件回调
     * <p>
     * 当缓存值过期时触发此方法
     * </p>
     *
     * @param cache 缓存实例
     * @param key   缓存键
     */
    @Override
    public void onExpire(Cache cache, Object key, Date e) {
        this.foreachListener((listener) -> {
            listener.calledExpire(cache.getName(), key, e);
        });
    }
}
