package com.tbs.schedule.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.schedule.AbstractDelayQueueAsyncBackgroundSchedule;
import com.tbs.schedule.IScheduleExecuteErrorHandle;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 本地异步定时计划器
 *
 * @author tongj
 */
@Slf4j
public class DelayQueueSchedule extends AbstractDelayQueueAsyncBackgroundSchedule<DelayedTaskWrapper> {

    public DelayQueueSchedule(IScheduleExecuteErrorHandle executeErrorHandle, Long pollTime, TimeUnit pollTimeUnit) {
        super(executeErrorHandle, pollTime, pollTimeUnit);
    }

    public DelayQueueSchedule(IScheduleExecuteErrorHandle executeErrorHandle) {
        this(executeErrorHandle, 5L, TimeUnit.MINUTES);
    }

    @Override
    protected DelayedTaskWrapper createTask(Runnable runnable, Duration delay) {
        AssertUtil.notNull(runnable, "runnable can not be null");
        if (delay == null) {
            delay = Duration.ZERO;
        }
        Date shouldRunTime = new Date(System.currentTimeMillis() + delay.toMillis());
        SimpleScheduleTask scheduleTask = new SimpleScheduleTask(shouldRunTime, runnable, this);
        return new DelayedTaskWrapper(scheduleTask);
    }
}
