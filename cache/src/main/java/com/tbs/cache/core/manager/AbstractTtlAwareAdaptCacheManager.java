package com.tbs.cache.core.manager;

import com.tbs.cache.core.ICacheListenner;
import com.tbs.cache.core.expire.ICacheExpiredActuator;
import com.tbs.common.IStartup;
import com.tbs.common.data.IFeatureWrapper;
import com.tbs.common.util.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 抽象TTL缓存管理器
 *
 * @author tongj
 */
public abstract class AbstractTtlAwareAdaptCacheManager
    implements CacheManager, IFeatureWrapper<CacheManager>, IStartup {

    private static final Logger log = LoggerFactory.getLogger(AbstractTtlAwareAdaptCacheManager.class);

    private List<ICacheListenner> cacheListenners;

    @Override
    public void startUp() throws RuntimeException {
        try {
            cacheListenners =
                SpringUtil.getBeansOfType(ICacheListenner.class).values().stream().collect(Collectors.toList());
            log.info("cache listenner init success. num:{}", cacheListenners.size());
            cacheListenners.forEach((listenner) -> {
                log.info("cache listenner use {}", listenner);
            });
        } catch (Exception e) {
            log.error("cache listenner init error", e);
            cacheListenners = new ArrayList<>(1);
        }
        log.info("cache manager use {};", getOrginal());
        log.info("cache expired actuator use {};", getExpiredActuator());

    }

    /**
     * 获取缓存过期执行器
     *
     * @return
     */
    protected abstract ICacheExpiredActuator getExpiredActuator();

    @Override
    public Cache getCache(String name) {
        return new TtlAdaptCache(getOrginal().getCache(name), getExpiredActuator(), cacheListenners);
    }

    @Override
    public Collection<String> getCacheNames() {
        return getOrginal().getCacheNames();
    }

}
