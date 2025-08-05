package com.tbs.cache.core.impl;

import com.tbs.cache.config.CacheProperty;
import com.tbs.cache.core.manager.AbstractTtlAwareAdaptCacheManager;
import com.tbs.cache.core.expire.ICacheExpiredActuator;
import com.tbs.common.IStartup;
import com.tbs.common.util.SpringUtil;
import org.springframework.cache.CacheManager;

import javax.annotation.Resource;

/**
 * 导入的缓存管理器
 *
 * @author tongj
 */
public class PropertyInitTtlAwareAdaptCacheManager extends AbstractTtlAwareAdaptCacheManager implements IStartup {

    /**
     * 缓存属性
     */
    @Resource
    private CacheProperty cacheProperty;

    private CacheManager cacheManager;
    private ICacheExpiredActuator expiredActuator;

    @Override
    protected CacheManager getCacheManager() {
        return cacheManager;
    }

    @Override
    protected ICacheExpiredActuator getExpiredActuator() {
        return expiredActuator;
    }

    @Override
    public void startUp() throws RuntimeException {
        cacheManager = SpringUtil.getBean(cacheProperty.getCacheManagerImplementation());
        expiredActuator = SpringUtil.getBean(cacheProperty.getExpiredActuator());
        super.startUp();
    }
}
