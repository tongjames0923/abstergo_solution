package com.tbs.cache.config;

import com.tbs.cache.core.IExpiredActuator;
import com.tbs.cache.core.impl.LocalExpireActuator;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.stereotype.Component;

/**
 * @author tongj
 */
@ConfigurationProperties(prefix = "tbs.cache")
@Component
@Data
public class CacheProperty {
    /**
     * 缓存过期处理类
     */
    private Class<? extends IExpiredActuator> expiredActuator = LocalExpireActuator.class;

    /**
     * 缓存管理类实现
     */
    private Class<? extends CacheManager> cacheManagerImplementation = ConcurrentMapCacheManager.class;
}
