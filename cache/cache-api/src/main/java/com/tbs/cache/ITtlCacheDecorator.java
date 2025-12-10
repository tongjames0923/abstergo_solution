package com.tbs.cache;

import com.tbs.cache.integration.AbstractTtlCache;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * TTL缓存装饰器接口
 * <p>
 * 用于将普通的Spring Cache装饰成支持TTL(Time To Live)功能的缓存
 * 通过装饰模式扩展缓存的功能，添加过期时间支持
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public interface ITtlCacheDecorator {
    /**
     * 将普通缓存装饰成TTL缓存
     * <p>
     * 接收一个普通的Spring Cache实例和CacheManager，返回一个装饰后的AbstractTtlCache实例
     * </p>
     *
     * @param cache   要装饰的普通缓存实例
     * @param manager 缓存管理器
     * @return 装饰后的TTL缓存实例
     */
    AbstractTtlCache decorateCache(Cache cache, CacheManager manager);
}
