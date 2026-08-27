package com.tbs.cache.impl;

import com.tbs.cache.ITtlCacheDecorator;
import com.tbs.cache.integration.AbstractAdaptTtlCache;
import com.tbs.cache.integration.AbstractMultiLevelCache;
import com.tbs.cache.integration.AbstractTtlCache;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.framework.SpringUtil;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class MultiLevelAdaptTtlCache extends AbstractAdaptTtlCache {

    private List<AbstractTtlCache> caches = null;

    /**
     * 多级缓存过期拦截器
     */
    public static interface IMultiLevelCacheTtlInterceptor {
        /**
         * 拦截缓存过期时间
         *
         * @param cache
         * @param key
         * @param expireTime
         * @return
         */
        Date intercept(AbstractTtlCache cache, int level, Object key, Date expireTime);
    }

    /**
     * 构造函数，初始化原始缓存
     *
     * @param orginCache 原始Spring Cache实例
     */
    public MultiLevelAdaptTtlCache(AbstractMultiLevelCache orginCache, Collection<ITtlCacheDecorator> decorators,
        CacheManager cacheManager) {
        super(orginCache);
        caches = orginCache.getLevels().stream().map((cache) -> {
            for (ITtlCacheDecorator decorator : decorators) {
                if (decorator.support(cache, cacheManager)) {
                    return decorator.decorateCache(cache, cacheManager);
                }
            }
            AssertUtil.isTrue(true, "No cache decorator found for cache: {}", cache.getName());
            return null;
        }).collect(Collectors.toList());
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
        int i = 0;
        for (AbstractTtlCache c : caches) {
            Date o = SpringUtil.getBean(IMultiLevelCacheTtlInterceptor.class).intercept(c, ++i, null, expireTime);
            c.expireAll(cache, o == null ? expireTime : o);
        }
        return super.expireAll(cache, expireTime);
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
        int i = 0;
        for (AbstractTtlCache c : caches) {
            Date o = SpringUtil.getBean(IMultiLevelCacheTtlInterceptor.class).intercept(c, ++i, key, expireTime);
            c.expire(cache, key, o == null ? expireTime : o);
        }
        return super.expire(cache, key, expireTime);
    }
}
