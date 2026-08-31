package com.tbs.cache;

import com.tbs.cache.integration.AbstractTtlCache;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.Ordered;

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
public interface ITtlCacheDecorator extends Ordered {

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
    default boolean support(Cache cache, CacheManager manager) {
        return false;
    }

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

    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
