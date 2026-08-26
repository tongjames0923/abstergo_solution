package com.tbs.cache.impl;

import com.tbs.cache.ITtlCacheDecorator;
import com.tbs.cache.integration.AbstractTtlCache;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

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
@Component
public class DefaultTtlCacheDecorator implements ITtlCacheDecorator {

    /**
     * 判断当前缓存是否支持TTL功能
     * <p>
     * 由于Spring Cache的限制，无法直接在Cache接口层面添加TTL功能 因此需要通过Cache的实现类或者CacheManager的实现类进行判断
     * </p>
     *
     * @param cache   要判断的缓存实例
     * @param manager 缓存管理器
     * @return 如果当前缓存支持TTL功能则返回true，否则返回false
     */
    @Override
    public boolean support(Cache cache, CacheManager manager) {
        return true;
    }

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
