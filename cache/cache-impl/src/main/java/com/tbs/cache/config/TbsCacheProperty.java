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

@ConfigurationProperties(prefix = "tbs.cache")
@Component
@Data
public class TbsCacheProperty {
    /**
     * 缓存管理器实现
     */
    private Class<? extends CacheManager> cacheManagerImpl = ConcurrentMapCacheManager.class;

    /**
     * ttl缓存装饰器实现
     */
    private Class<? extends ITtlCacheDecorator> ttlCacheDecorator = DefaultTtlCacheDecorator.class;

    /**
     * 缓存操作记录器实现
     */
    private Class<? extends ICacheOperationRecorder> recorder = SimpleCacheOperationRecorder.class;

    /**
     * 手动缓存实现
     */
    private Class<? extends IManualCacheHandler> manualCacheHandler = MapCacheHandler.class;

    /**
     * 缓存限制器实现
     */
    private Class<? extends ICacheLimiter> cacheLimiter = SimpleCacheLimiter.class;
}
