package com.tbs.cache.config;

import com.tbs.cache.ITtlCacheDecorator;
import com.tbs.cache.impl.DefaultTtlAdaptCacheMananger;
import com.tbs.cache.impl.DefaultTtlCacheDecorator;
import com.tbs.cache.integration.AbstractTtlAdaptCacheMananger;
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

}
