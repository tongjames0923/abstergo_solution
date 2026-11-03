package com.tbs.cache.impl;

import com.tbs.cache.expire.impl.TaskSchedulerCacheExpiredActuator;
import com.tbs.cache.integration.AbstractAdaptTtlCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;

/**
 * 基于调度器的TTL缓存适配器
 * <p>
 * 继承自AbstractAdaptTtlCache，通过调度器实现缓存的TTL（Time To Live）功能 使用TaskScheduler接口实现缓存条目的定时过期
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
@Slf4j
public class AdaptScheduleExpireTtlCache extends AbstractAdaptTtlCache {
    /**
     * 构造方法
     * <p>
     * 创建AdaptScheduleExpireTtlCache实例，包装原始缓存
     * </p>
     *
     * @param orginCache 要包装的原始缓存实例
     */
    public AdaptScheduleExpireTtlCache(Cache orginCache) {
        super(orginCache, new TaskSchedulerCacheExpiredActuator());
    }

}
