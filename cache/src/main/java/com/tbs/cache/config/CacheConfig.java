package com.tbs.cache.config;

import com.tbs.cache.config.property.CacheProperty;
import com.tbs.cache.impl.manager.AbstractContextCacheManager;
import com.tbs.cache.impl.manager.SimpleCcontextCacheManager;
import com.tbs.common.util.AssertUtil;
import com.tbs.common.util.BeanUtil;
import com.tbs.common.util.ExceptionUtil;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
@EnableCaching
public class CacheConfig {

    @Resource
    private CacheProperty cacheProperty;

    @Bean
    AbstractContextCacheManager cacheManager() {
        return BeanUtil.buildBeanFromProperties(() -> {
            return new SimpleCcontextCacheManager(cacheProperty.getCacheNameContextId(),
                cacheProperty.getCacheContextId(), cacheProperty.isAcceptNull());
        }, cacheProperty.getCacheManagerClass(), (c) -> {
            try {
                return c.getConstructor(String.class, String.class, boolean.class)
                    .newInstance(cacheProperty.getCacheNameContextId(), cacheProperty.getCacheContextId(),
                        cacheProperty.isAcceptNull());
            } catch (Throwable e) {
                AssertUtil.throwError(() -> ExceptionUtil.newException(e, "缓存管理器构建失败"));
            }
            return null;
        });
    }
}
