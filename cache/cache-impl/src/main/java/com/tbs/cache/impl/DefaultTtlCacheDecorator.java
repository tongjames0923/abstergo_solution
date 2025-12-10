package com.tbs.cache.impl;

import com.tbs.cache.ITtlCacheDecorator;
import com.tbs.cache.integration.AbstractTtlCache;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * 默认TTL缓存装饰器实现类
 * <p>
 * 实现ITtlCacheDecorator接口，将普通缓存装饰为支持TTL功能的缓存
 * 使用AdaptScheduleExpireTtlCache作为装饰后的缓存实现
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public class DefaultTtlCacheDecorator implements ITtlCacheDecorator {
    /**
     * 将普通缓存装饰为支持TTL功能的缓存
     * <p>
     * 创建AdaptScheduleExpireTtlCache实例来包装原有的缓存，添加TTL功能
     * </p>
     *
     * @param cache   要装饰的普通缓存实例
     * @param manager 缓存管理器（当前实现未使用）
     * @return 装饰后的TTL缓存实例
     */
    @Override
    public AbstractTtlCache decorateCache(Cache cache, CacheManager manager) {
        return new AdaptScheduleExpireTtlCache(cache);
    }
}
