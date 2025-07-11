package com.tbs.cache.config.property;

import com.tbs.cache.impl.manager.AbstractContextCacheManager;
import com.tbs.cache.impl.manager.SimpleCcontextCacheManager;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author abstergo
 */
@ConfigurationProperties(prefix = "tbs.cache")
@Component
@Data
public class CacheProperty {
    private String cacheContextId = "";
    private String cacheNameContextId = "";
    private Class<? extends AbstractContextCacheManager> cacheManagerClass = SimpleCcontextCacheManager.class;
    private boolean acceptNull = true;
}
