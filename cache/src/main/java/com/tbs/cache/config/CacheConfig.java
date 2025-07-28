package com.tbs.cache.config;

import com.tbs.cache.constant.CacheBeanNameConstant;
import com.tbs.cache.core.AbstractTtlAwareAdaptCacheManager;
import com.tbs.cache.core.IExpiredActuator;
import com.tbs.cache.core.impl.ImportedTtlAwareAdaptCacheManager;
import com.tbs.cache.core.impl.LocalExpireActuator;
import com.tbs.common.util.BeanUtil;
import com.tbs.schedule.ISchedule;
import com.tbs.schedule.IScheduleExecuteErrorHandle;
import com.tbs.schedule.impl.LocalAsyncSchedule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
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

    @Bean(CacheBeanNameConstant.CORE_CACHE_MANAGER)
    @ConditionalOnMissingBean(value = CacheManager.class, name = CacheBeanNameConstant.CORE_CACHE_MANAGER)
    CacheManager cacheManager() {
        return BeanUtil.buildBeanFromProperties(() -> {
            return new ConcurrentMapCacheManager();
        }, cacheProperty.getCacheManagerImplementation(), BeanUtil::buildByEmptyArgs);
    }

    @Bean
    @ConditionalOnMissingBean(value = AbstractTtlAwareAdaptCacheManager.class,
        name = CacheBeanNameConstant.TTL_AWARE_CACHE_MANAGER)
    @Primary
    public AbstractTtlAwareAdaptCacheManager ttlAwareAdaptCacheManager(
        @Qualifier(CacheBeanNameConstant.CORE_CACHE_MANAGER) CacheManager cacheManager,
        IExpiredActuator expiredActuator) {
        return new ImportedTtlAwareAdaptCacheManager(cacheManager, expiredActuator);
    }


    @Bean
    @ConditionalOnMissingBean(ISchedule.class)
    public ISchedule expireSchedule(IScheduleExecuteErrorHandle scheduleExecuteErrorHandle) {
        return new LocalAsyncSchedule(
            scheduleExecuteErrorHandle);
    }

}
