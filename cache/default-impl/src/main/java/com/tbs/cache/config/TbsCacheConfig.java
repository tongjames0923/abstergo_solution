package com.tbs.cache.config;

import com.tbs.cache.ITtlCacheDecorator;
import com.tbs.cache.impl.DefaultTtlAdaptCacheMananger;
import com.tbs.cache.impl.DefaultTtlCacheDecorator;
import com.tbs.cache.integration.AbstractTtlAdaptCacheMananger;
import com.tbs.cache.limit.ICacheLimiter;
import com.tbs.cache.limit.ICacheOperationRecorder;
import com.tbs.cache.limit.impl.SimpleCacheLimiter;
import com.tbs.cache.limit.impl.SimpleCacheOperationRecorder;
import com.tbs.cache.manual.IManualCacheHandler;
import com.tbs.cache.manual.impl.MapCacheHandler;
import com.tbs.common.util.base.BeanUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.annotation.Resource;

@Configuration
@EnableCaching
public class TbsCacheConfig {

    @Resource
    TbsCacheProperty cacheProperty;

    @Bean
    @Primary
    AbstractTtlAdaptCacheMananger adaptCacheMananger() {
        return new DefaultTtlAdaptCacheMananger(cacheProperty.getCacheManagerImpl());
    }

    @Bean
    @ConditionalOnMissingBean(ITtlCacheDecorator.class)
    ITtlCacheDecorator ttlCacheDecorator() {
        return BeanUtil.buildBeanFromProperties(DefaultTtlCacheDecorator::new, cacheProperty.getTtlCacheDecorator(),
            BeanUtil::buildByEmptyArgs);
    }

    @Bean
    @ConditionalOnMissingBean(ICacheOperationRecorder.class)
    ICacheOperationRecorder cacheOperationRecorder() {
        return BeanUtil.buildBeanFromProperties(SimpleCacheOperationRecorder::new, cacheProperty.getRecorder(),
            BeanUtil::buildByEmptyArgs);
    }

    @Bean
    @ConditionalOnMissingBean(IManualCacheHandler.class)
    IManualCacheHandler manualCacheHandler() {
        return BeanUtil.buildBeanFromProperties(MapCacheHandler::new, cacheProperty.getManualCacheHandler(),
            BeanUtil::buildByEmptyArgs);
    }

    @Bean
    @ConditionalOnMissingBean(ICacheLimiter.class)
    ICacheLimiter cacheLimiter() {
        return BeanUtil.buildBeanFromProperties(SimpleCacheLimiter::new, cacheProperty.getCacheLimiter(),
            BeanUtil::buildByEmptyArgs);
    }

}
