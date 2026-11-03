package com.tbs.cache.expire.impl;

import com.tbs.cache.expire.ICacheExpiredActuator;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.schedule.api.TaskScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;

import java.util.Date;

@Slf4j
public class TaskSchedulerCacheExpiredActuator implements ICacheExpiredActuator {

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
            var h = getSchedule().submit(CacheExpireTaskExecutor.build(cache.getName(), null, expireTime));
            if (h == null) {
                return false;
            }
            return StrUtil.isNotEmpty(h.getTaskId());
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
    public boolean expire(Cache cache, Object key, Date expireTime) {
        try {
            var h = getSchedule().submit(CacheExpireTaskExecutor.build(cache.getName(), key, expireTime));
            if (h == null) {
                return false;
            }
            return StrUtil.isNotEmpty(h.getTaskId());
        } catch (Exception e) {
            log.error("expireAll error", e);
            return false;
        }
    }
}
