package com.tbs.cache.impl;

import com.tbs.cache.ITtlCacheDecorator;
import com.tbs.cache.integration.AbstractTtlAdaptCacheMananger;
import com.tbs.cache.integration.AbstractTtlCache;
import com.tbs.common.util.SpringUtil;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * 默认的TTL缓存管理器
 *
 * @author tongj
 */
public class DefaultTtlAdaptCacheMananger extends AbstractTtlAdaptCacheMananger {

    private Class<? extends CacheManager> orginCacheManagerClass;

    private CacheManager orginCacheManager;

    public DefaultTtlAdaptCacheMananger(Class<? extends CacheManager> orginCacheManager) {
        this.orginCacheManagerClass = orginCacheManager;
    }

    @Override
    protected AbstractTtlCache decorateCacheToTtl(Cache cache) {
        return SpringUtil.getBean(ITtlCacheDecorator.class).decorateCache(cache, getOrginal());
    }

    @Override
    public CacheManager getOrginal() {
        if (orginCacheManager == null) {
            synchronized (this) {
                if (orginCacheManager == null) {
                    orginCacheManager = SpringUtil.getBean(orginCacheManagerClass);
                }
            }
        }
        return orginCacheManager;
    }

    @Override
    public void setOrginal(CacheManager orginal) {
        this.orginCacheManager = orginal;
    }
}
