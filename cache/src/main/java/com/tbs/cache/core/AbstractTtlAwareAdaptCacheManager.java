package com.tbs.cache.core;

import com.tbs.common.util.AssertUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

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

    @Resource
    Map<String, ICacheListenner> cacheListennerMap;

    private List<ICacheListenner> cacheListenners;

    @PostConstruct
    void init() {
        cacheListenners = cacheListennerMap.values().stream().collect(Collectors.toList());
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
        return new TtlAdaptCache(getCacheManager().getCache(name), cacheListenners);
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
