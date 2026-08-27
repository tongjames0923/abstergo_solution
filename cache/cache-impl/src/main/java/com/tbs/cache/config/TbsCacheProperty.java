package com.tbs.cache.config;

import com.tbs.cache.ITtlCacheDecorator;
import com.tbs.cache.impl.DefaultTtlCacheDecorator;
import com.tbs.cache.limit.ICacheLimiter;
import com.tbs.cache.limit.ICacheOperationRecorder;
import com.tbs.cache.limit.impl.SimpleCacheLimiter;
import com.tbs.cache.limit.impl.SimpleCacheOperationRecorder;
import com.tbs.cache.manual.IManualCacheHandler;
import com.tbs.cache.manual.impl.MapCacheHandler;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Tbs缓存属性配置类
 * <p>
 * 用于绑定tbs.cache前缀的配置属性
 * 提供缓存相关组件的实现类配置
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "tbs.cache")
@Component
@Data
public class TbsCacheProperty {
    /**
     * 缓存管理器实现类
     * <p>
     * 指定使用的CacheManager实现类
     * 默认使用ConcurrentMapCacheManager
     * </p>
     */
    private Class<? extends CacheManager> cacheManagerImpl = ConcurrentMapCacheManager.class;

    /**
     * TTL缓存装饰器实现类
     * <p>
     * 指定使用的ITtlCacheDecorator实现类
     * 默认使用DefaultTtlCacheDecorator
     * </p>
     */
    private Class<? extends ITtlCacheDecorator> ttlCacheDecorator = DefaultTtlCacheDecorator.class;

    /**
     * 缓存操作记录器实现类
     * <p>
     * 指定使用的ICacheOperationRecorder实现类
     * 默认使用SimpleCacheOperationRecorder
     * </p>
     */
    private Class<? extends ICacheOperationRecorder> recorder = SimpleCacheOperationRecorder.class;

    /**
     * 手动缓存处理器实现类
     * <p>
     * 指定使用的IManualCacheHandler实现类
     * 默认使用MapCacheHandler
     * </p>
     */
    private Class<? extends IManualCacheHandler> manualCacheHandler = MapCacheHandler.class;

    /**
     * 缓存限制器实现类
     * <p>
     * 指定使用的ICacheLimiter实现类
     * 默认使用SimpleCacheLimiter
     * </p>
     */
    private Class<? extends ICacheLimiter> cacheLimiter = SimpleCacheLimiter.class;

    /**
     * TTL 策略配置
     */
    private TtlConfig ttl = new TtlConfig();

    /**
     * TTL 策略配置
     */
    @Data
    public static class TtlConfig {

        /**
         * 按 cacheName 的 TTL 覆盖
         * <p>
         * key 为业务声明的原始 cacheName（不带 SYSTEM:CACHE: 前缀）， value 为该缓存的 TTL。
         * </p>
         */
        private Map<String, Duration> overrides = new HashMap<>();
    }


}
