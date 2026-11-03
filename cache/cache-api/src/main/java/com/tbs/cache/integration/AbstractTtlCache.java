package com.tbs.cache.integration;

import com.tbs.cache.ICacheListenner;
import com.tbs.cache.IListenable;
import com.tbs.cache.expire.ICacheExpiredActuator;
import com.tbs.cache.expire.IExpireDetailsManager;
import com.tbs.cache.expire.ITtlIntercepter;
import com.tbs.cache.expire.vo.ExpireDetails;
import com.tbs.common.data.IFeatureWrapper;
import com.tbs.common.util.framework.SpringUtil;
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
public abstract class AbstractTtlCache implements Cache, IFeatureWrapper<Cache>, IListenable {

    private volatile List<ICacheListenner> listeners;

    private ICacheExpiredActuator targetedCacheExpiredActuator;

    /**
     * 获取目标缓存过期执行器
     *
     * @return
     */
    public abstract ICacheExpiredActuator getTargetedCacheExpiredActuator();

    /**
     * 获取监听器
     *
     * @return
     */
    public abstract List<ICacheListenner> getListeners();

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
            getTargetedCacheExpiredActuator().expireAll(AbstractTtlCache.this, date);
            allExpired.set(true);
        });
        if (!allExpired.get().orElse(false)) {
            isCallToExpire(key).ifPresent((date) -> {
                getTargetedCacheExpiredActuator().expire(AbstractTtlCache.this, key, date);
            });
        }
    }

    @Override
    public void evict(Object key) {
        onEvict(this, key);
        getOriginal().evict(key);
        // 必须在真正清除之后派发：监听器在此回读得到的是已失效状态，级联失效与变更通知都依赖这个顺序
        onEvicted(this, key);
    }

    @Override
    public void clear() {
        onClear(this);
        getOriginal().clear();
        onCleared(this);
    }

    /**
     * 静默清除：直达被包装的原始缓存，不触发任何监听回调
     * <p>
     * 供关联缓存级联失效使用。级联必须走这里，否则目标缓存的清除会再次触发 {@link #onEvicted(Cache, Object)} 并展开它自身的关联声明，使声明链在运行期形成递归。
     * 多级缓存下本方法同样有静默效果：原始缓存为多层编排实例，其遍历的层级是未经监听包装的 基础缓存实例，因此不会重新进入回调链。
     * </p>
     * <p>
     * 代价是级联不传递：A 的关联只清 B，不会继续清 B 的关联，多级依赖须由声明方显式展开。
     * </p>
     *
     * @param key 缓存键
     */
    public void evictSilently(Object key) {
        getOriginal().evict(key);
    }

    /**
     * 静默清空，语义同 {@link #evictSilently(Object)}
     */
    public void clearSilently() {
        getOriginal().clear();
    }

    /**
     * 外部调用请求缓存过期
     *
     * @param key
     * @param expireTime
     */
    public void callToExpire(Object key, Date expireTime) {
        var expireTimeWrapper = com.tbs.common.data.ValueWrapper.of(expireTime);
        SpringUtil.getBeansByTypeSafe(ITtlIntercepter.class).forEach((intercepter) -> {
            expireTimeWrapper.set(intercepter.onTtlExpired(this, key, expireTime));
        });
        getExpireDetailsManager().addExpireDetails(this.getName(),
            new ExpireDetails(expireTimeWrapper.get().orElse(expireTime), false, key));
    }

    /**
     * 外部调用请求缓存过期
     *
     * @param expireTime
     */
    public void callToExpireAll(Date expireTime) {
        var expireTimeWrapper = com.tbs.common.data.ValueWrapper.of(expireTime);
        SpringUtil.getBeansByTypeSafe(ITtlIntercepter.class).forEach((intercepter) -> {
            expireTimeWrapper.set(intercepter.onTtlExpired(this, null, expireTime));
        });
        getExpireDetailsManager().addExpireDetails(this.getName(),
            new ExpireDetails(expireTimeWrapper.get().orElse(expireTime), false, null));
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
        this.foreachListener((listener) -> {
            listener.onEvict(cache.getName(), key);
        });
    }

    /**
     * 删除完成回调
     * <p>
     * 与 {@link #onEvict(Cache, Object)} 同样经 {@link #foreachListener} 派发：异常隔离（单个监听器 失败不影响其他监听器，也不上抛中断业务写入）与
     * {@code support} 过滤两处语义必须一致， 不能只在删除路径上例外。
     * </p>
     */
    @Override
    public void onEvicted(Cache cache, Object key) {
        this.foreachListener((listener) -> {
            listener.onEvicted(cache.getName(), key);
        });
    }

    @Override
    public void onCleared(Cache cache) {
        this.foreachListener((listener) -> {
            listener.onCleared(cache.getName());
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
