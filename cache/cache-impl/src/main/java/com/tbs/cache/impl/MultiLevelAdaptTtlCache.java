package com.tbs.cache.impl;

import com.tbs.cache.ITtlCacheDecorator;
import com.tbs.cache.integration.AbstractAdaptTtlCache;
import com.tbs.cache.integration.AbstractMultiLevelCache;
import com.tbs.cache.integration.AbstractTtlCache;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.framework.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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
        log.debug("[MultiLevelTtl] 初始化多级缓存: cacheName={}, levelCount={}, decoratorCount={}",
            orginCache.getName(), orginCache.getLevels().size(), decorators.size());
        caches = orginCache.getLevels().stream().map((cache) -> {
            for (ITtlCacheDecorator decorator : decorators) {
                if (decorator.support(cache, cacheManager)) {
                    log.debug("[MultiLevelTtl] cacheName={}, 层级缓存 {} 匹配装饰器: {}", cache.getName(),
                        cache.getClass().getSimpleName(), decorator.getClass().getSimpleName());
                    return decorator.decorateCache(cache, cacheManager);
                }
            }
            AssertUtil.isTrue(true, "No cache decorator found for cache: {}", cache.getName());
            return null;
        }).collect(Collectors.toList());
        log.debug("[MultiLevelTtl] 初始化完成: cacheName={}, 已装饰层级数={}", orginCache.getName(), caches.size());
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
        log.debug("[MultiLevelTtl] expireAll 开始: cacheName={}, expireTime={}, levelCount={}", cache.getName(),
            expireTime, caches.size());
        int i = 0;
        for (AbstractTtlCache c : caches) {
            int level = ++i;
            Date o = SpringUtil.getBean(IMultiLevelCacheTtlInterceptor.class).intercept(c, level, null, expireTime);
            Date finalExpireTime = o == null ? expireTime : o;
            log.debug(
                "[MultiLevelTtl] expireAll: cacheName={}, level={}, cacheClass={}, interceptedExpireTime={}, finalExpireTime={}",
                cache.getName(), level, c.getClass().getSimpleName(), o, finalExpireTime);
            c.expireAll(cache, finalExpireTime);
        }
        boolean result = super.expireAll(cache, expireTime);
        log.debug("[MultiLevelTtl] expireAll 完成: cacheName={}, result={}", cache.getName(), result);
        return result;
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
        log.debug("[MultiLevelTtl] expire 开始: cacheName={}, key={}, expireTime={}, levelCount={}", cache.getName(),
            key, expireTime, caches.size());
        int i = 0;
        for (AbstractTtlCache c : caches) {
            int level = ++i;
            Date o = SpringUtil.getBean(IMultiLevelCacheTtlInterceptor.class).intercept(c, level, key, expireTime);
            Date finalExpireTime = o == null ? expireTime : o;
            log.debug(
                "[MultiLevelTtl] expire: cacheName={}, key={}, level={}, cacheClass={}, interceptedExpireTime={}, finalExpireTime={}",
                cache.getName(), key, level, c.getClass().getSimpleName(), o, finalExpireTime);
            c.expire(cache, key, finalExpireTime);
        }
        boolean result = super.expire(cache, key, expireTime);
        log.debug("[MultiLevelTtl] expire 完成: cacheName={}, key={}, result={}", cache.getName(), key, result);
        return result;
    }
}
