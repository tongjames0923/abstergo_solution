package com.tbs.cache.config;

import com.tbs.cache.expire.IExpireDetailsManager;
import com.tbs.cache.expire.impl.LocalMapExpireDetailsManager;
import com.tbs.cache.impl.DefaultTtlAdaptCacheMananger;
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

/**
 * Tbs缓存配置类
 * <p>
 * 配置缓存相关的Bean，包括缓存管理器、装饰器、操作记录器等
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
@Configuration
@EnableCaching
public class TbsCacheConfig {

    /**
     * Tbs缓存属性配置
     */
    @Resource
    TbsCacheProperty cacheProperty;

    /**
     * 创建并配置TTL适配缓存管理器
     * <p>
     * 使用TbsCacheProperty中的cacheManagerImpl配置创建默认的TTL适配缓存管理器
     * 标记为@Primary，表示这是主要的缓存管理器
     * </p>
     *
     * @return 配置好的TTL适配缓存管理器
     */
    @Bean
    @Primary
    AbstractTtlAdaptCacheMananger adaptCacheMananger() {
        return new DefaultTtlAdaptCacheMananger(cacheProperty.getCacheManagerImpl());
    }


    @Bean
    @ConditionalOnMissingBean(IExpireDetailsManager.class)
    IExpireDetailsManager expireDetailsManager() {
        return new LocalMapExpireDetailsManager();
    }

    /**
     * 创建并配置缓存操作记录器
     * <p>
     * 如果容器中不存在ICacheOperationRecorder实例，则创建默认的SimpleCacheOperationRecorder
     * 使用TbsCacheProperty中的recorder配置进行初始化
     * </p>
     *
     * @return 配置好的缓存操作记录器
     */
    @Bean
    @ConditionalOnMissingBean(ICacheOperationRecorder.class)
    ICacheOperationRecorder cacheOperationRecorder() {
        return BeanUtil.buildBeanFromProperties(SimpleCacheOperationRecorder::new, cacheProperty.getRecorder(),
            BeanUtil::buildByEmptyArgs);
    }

    /**
     * 创建并配置手动缓存处理器
     * <p>
     * 如果容器中不存在IManualCacheHandler实例，则创建默认的MapCacheHandler
     * 使用TbsCacheProperty中的manualCacheHandler配置进行初始化
     * </p>
     *
     * @return 配置好的手动缓存处理器
     */
    @Bean
    @ConditionalOnMissingBean(IManualCacheHandler.class)
    IManualCacheHandler manualCacheHandler() {
        return BeanUtil.buildBeanFromProperties(MapCacheHandler::new, cacheProperty.getManualCacheHandler(),
            BeanUtil::buildByEmptyArgs);
    }

    /**
     * 创建并配置缓存限制器
     * <p>
     * 如果容器中不存在ICacheLimiter实例，则创建默认的SimpleCacheLimiter
     * 使用TbsCacheProperty中的cacheLimiter配置进行初始化
     * </p>
     *
     * @return 配置好的缓存限制器
     */
    @Bean
    @ConditionalOnMissingBean(ICacheLimiter.class)
    ICacheLimiter cacheLimiter() {
        return BeanUtil.buildBeanFromProperties(SimpleCacheLimiter::new, cacheProperty.getCacheLimiter(),
            BeanUtil::buildByEmptyArgs);
    }

}
