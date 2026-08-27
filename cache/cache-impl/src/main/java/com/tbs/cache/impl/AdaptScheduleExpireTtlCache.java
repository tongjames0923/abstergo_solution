package com.tbs.cache.impl;

import com.tbs.cache.integration.AbstractAdaptTtlCache;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.schedule.api.TaskScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;

import java.time.Duration;
import java.util.Date;

/**
 * 基于调度器的TTL缓存适配器
 * <p>
 * 继承自AbstractAdaptTtlCache，通过调度器实现缓存的TTL（Time To Live）功能
 * 使用TaskScheduler接口实现缓存条目的定时过期
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
        super(orginCache);
    }

    /**
     * 获取调度器实例
     * <p>
     * 从Spring容器中获取TaskScheduler实例
     * </p>
     *
     * @return TaskScheduler调度器实例
     */
    protected TaskScheduler getSchedule() {
        return SpringUtil.getBean(TaskScheduler.class);
    }

    /**
     * 设置所有缓存条目的过期时间
     * <p>
     * 使用调度器在指定时间清空整个缓存
     * </p>
     *
     * @param cache      要设置过期时间的缓存实例
     * @param expireTime 过期时间
     * @return 如果调度任务成功添加则返回true，否则返回false
     */
    @Override
    public boolean expireAll(Cache cache, Date expireTime) {
        try {
            getSchedule().schedule(() -> cache.clear(),
                Duration.between(new Date().toInstant(), expireTime.toInstant()));
            return super.expireAll(cache, expireTime);
        } catch (Exception e) {
            log.error("expireAll error", e);
            return false;
        }
    }

    /**
     * 设置单个缓存条目的过期时间
     * <p>
     * 使用调度器在指定时间删除特定键的缓存条目
     * </p>
     *
     * @param cache      要设置过期时间的缓存实例
     * @param key        要设置过期时间的缓存键
     * @param expireTime 过期时间
     * @return 如果调度任务成功添加则返回true，否则返回false
     */
    @Override
    public boolean expire( Cache cache, Object key,  Date expireTime) {
        try {
            getSchedule().schedule(() -> cache.evict(key),
                Duration.between(new Date().toInstant(), expireTime.toInstant()));
            return super.expire(cache, key, expireTime);
        } catch (Exception e) {
            log.error("expireAll error", e);
            return false;
        }
    }
}
