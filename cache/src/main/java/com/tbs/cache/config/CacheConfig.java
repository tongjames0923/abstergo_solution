package com.tbs.cache.config;

import com.tbs.cache.core.manager.AbstractTtlAwareAdaptCacheManager;
import com.tbs.cache.core.expire.ICacheExpiredActuator;
import com.tbs.cache.core.impl.LocalExpireActuatorCache;
import com.tbs.cache.core.impl.PropertyInitTtlAwareAdaptCacheManager;
import com.tbs.common.util.BeanUtil;
import com.tbs.schedule.ISchedule;
import com.tbs.schedule.IScheduleExecuteErrorHandle;
import com.tbs.schedule.impl.DelayQueueSchedule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.annotation.EnableCaching;
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
    @ConditionalOnMissingBean(value = ICacheExpiredActuator.class)
    public ICacheExpiredActuator expiredActuator() {
        return BeanUtil.buildBeanFromProperties(() -> {
            return new LocalExpireActuatorCache();
        }, cacheProperty.getExpiredActuator(), BeanUtil::buildByEmptyArgs);
    }

    @Bean
    @ConditionalOnMissingBean(value = AbstractTtlAwareAdaptCacheManager.class)
    @Primary
    public AbstractTtlAwareAdaptCacheManager ttlAwareAdaptCacheManager() {
        return new PropertyInitTtlAwareAdaptCacheManager();
    }

    @Bean
    @ConditionalOnMissingBean(ISchedule.class)
    public ISchedule expireSchedule(IScheduleExecuteErrorHandle scheduleExecuteErrorHandle) {
        return new DelayQueueSchedule(scheduleExecuteErrorHandle);
    }

}
