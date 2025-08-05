package com.tbs.cache.core.expire;

import com.tbs.schedule.ISchedule;

import javax.annotation.Resource;

/**
 * 抽象的Spring管理的缓存的过期处理
 *
 * @author tongj
 */
public abstract class AbstractSpringScheduleExpiredActuator<RAW> extends AbstractScheduleExpiredActuator<RAW> {
    @Resource
    private ISchedule schedule;

    @Override
    protected ISchedule getSchedule() {
        return schedule;
    }
}
