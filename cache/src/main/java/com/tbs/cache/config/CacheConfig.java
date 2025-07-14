package com.tbs.cache.config;

import com.tbs.cache.core.AbstractTtlAwareAdaptCacheManager;
import com.tbs.cache.core.IExpiredActuator;
import com.tbs.cache.core.impl.ImportedTtlAwareAdaptCacheManager;
import com.tbs.cache.core.impl.LocalExpireActuator;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.util.AssertUtil;
import com.tbs.common.util.BeanUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.annotation.Resource;

/**
 * 缓存配置
 *
 * @author tongj
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Resource
    CacheProperty cacheProperty;

    @Bean
    @ConditionalOnMissingBean(value = IExpiredActuator.class)
    public IExpiredActuator expiredActuator() {
        return BeanUtil.buildBeanFromProperties(() -> {
            return new LocalExpireActuator();
        }, cacheProperty.getExpiredActuator(), BeanUtil::buildByEmptyArgs);
    }

    @Bean("cacheManagerImpl")
    CacheManager cacheManager() {
        return BeanUtil.buildBeanFromProperties(() -> {
            return new ConcurrentMapCacheManager();
        }, cacheProperty.getCacheManagerImplementation(), (c) -> {
            try {
                return c.getConstructor().newInstance();
            } catch (Exception e) {
                AssertUtil.throwError(() -> new FormattedRuntimeException(e, "构建CacheManager失败"));
            }
            return null;
        });
    }

    @Bean
    @Primary
    public AbstractTtlAwareAdaptCacheManager cacheManager(@Qualifier("cacheManagerImpl") CacheManager cacheManager,
        IExpiredActuator expiredActuator) {
        return new ImportedTtlAwareAdaptCacheManager(cacheManager, expiredActuator);
    }

}
