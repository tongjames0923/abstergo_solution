package com.tbs.cache.core.manager;

import com.tbs.cache.core.ICacheListenner;
import com.tbs.cache.core.expire.ICacheExpiredActuator;
import com.tbs.common.IStartup;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 抽象TTL缓存管理器
 *
 * @author tongj
 */
public abstract class AbstractTtlAwareAdaptCacheManager implements CacheManager, IStartup {

    private static final Logger log = LoggerFactory.getLogger(AbstractTtlAwareAdaptCacheManager.class);

    @Data
    @AllArgsConstructor
    public static final class MethodExpireInfo {
        private String cacheName;
        private Object key;
        private long expireTimeMillis;
        private boolean expireAll = false;
        private String methodName;
    }

    @Resource
    Map<String, ICacheListenner> cacheListennerMap;

    private List<ICacheListenner> cacheListenners;

    @Override
    public void startUp() throws RuntimeException {
        cacheListenners = cacheListennerMap.values().stream().collect(Collectors.toList());
        log.info("cache manager use {};",getCacheManager());
        log.info("cache expired actuator use {};",getExpiredActuator());
        cacheListenners.forEach((listenner) -> {
            log.info("cache listenner use {}", listenner);
        });
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
    protected abstract ICacheExpiredActuator getExpiredActuator();

    @Override
    public Cache getCache(String name) {
        return new TtlAdaptCache(getCacheManager().getCache(name), getExpiredActuator(), cacheListenners);
    }

    @Override
    public Collection<String> getCacheNames() {
        return getCacheManager().getCacheNames();
    }

}
