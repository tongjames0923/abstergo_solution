package com.tbs.cache.config;

import com.tbs.cache.ITtlCacheDecorator;
import com.tbs.cache.impl.DefaultTtlCacheDecorator;
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
}
