package com.tbs.cache.impl;

import com.tbs.cache.IMultiLevelSupport;
import com.tbs.cache.ITtlCacheDecorator;
import com.tbs.cache.integration.AbstractAdaptTtlCache;
import com.tbs.cache.integration.AbstractMultiLevelCache;
import com.tbs.cache.integration.AbstractTtlCache;
import com.tbs.common.error.AssertUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class MultiLevelAdaptTtlCache extends AbstractAdaptTtlCache implements IMultiLevelSupport {

    private List<AbstractTtlCache> caches = null;

    /**
     * 构造函数，初始化原始缓存
     *
     * @param orginCache 原始Spring Cache实例
     */
    public MultiLevelAdaptTtlCache(AbstractMultiLevelCache orginCache, Collection<ITtlCacheDecorator> decorators,
        CacheManager cacheManager) {
        super(orginCache, new MultiLevelCacheExpiredActuator());
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

    @Override
    public List<Cache> getLevels() {
        return caches.stream().map(cache -> (Cache)cache).toList();
    }

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
        Date intercept(Cache cache, int level, Object key, Date expireTime);
    }

}
