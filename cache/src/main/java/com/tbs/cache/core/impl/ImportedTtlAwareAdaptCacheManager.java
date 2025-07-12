package com.tbs.cache.core.impl;

import com.tbs.cache.core.AbstractTtlAwareAdaptCacheManager;
import com.tbs.cache.core.IExpiredActuator;
import com.tbs.common.holder.context.IContext;
import com.tbs.common.util.AssertUtil;
import org.springframework.cache.CacheManager;

import java.util.Date;

/**
 * 导入的缓存管理器
 *
 * @author tongj
 */
public class ImportedTtlAwareAdaptCacheManager extends AbstractTtlAwareAdaptCacheManager {

    private CacheManager cacheManager;
    private IExpiredActuator expiredActuator;

    public ImportedTtlAwareAdaptCacheManager(CacheManager cacheManager, IExpiredActuator expiredActuator) {
        AssertUtil.notNull(cacheManager, "cacheManager is null");
        this.cacheManager = cacheManager;
        AssertUtil.notNull(expiredActuator, "expiredActuator is null");
        this.expiredActuator = expiredActuator;
    }

    @Override
    protected CacheManager getCacheManager() {
        return cacheManager;
    }

    @Override
    protected IExpiredActuator getExpiredActuator() {
        return expiredActuator;
    }
}
