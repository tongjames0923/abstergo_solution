package com.tbs.schedule.impl;

import com.tbs.schedule.AbstractTimingWheelAsyncBackgroundSchedule;
import com.tbs.schedule.IScheduleExecuteErrorHandle;
import com.tbs.schedule.IScheduleTask;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 时间轮定时器
 */
public class TimingWheelSchedule extends AbstractTimingWheelAsyncBackgroundSchedule {
    /**
     * 构造函数，初始化时间轮调度器
     *
     * @param executeErrorHandle 错误处理器
     * @param pollTime           轮询时间
     * @param pollTimeUnit       轮询时间单位
     * @param tickDuration       时间轮每个槽位的时间间隔
     * @param wheelSize          时间轮槽数量
     * @param timeUnit           时间单位
     */
    public TimingWheelSchedule(IScheduleExecuteErrorHandle executeErrorHandle, Long pollTime, TimeUnit pollTimeUnit,
        long tickDuration, int wheelSize, TimeUnit timeUnit) {
        super(executeErrorHandle, pollTime, pollTimeUnit, tickDuration, wheelSize, timeUnit);
    }

    @Override
    protected IScheduleTask createTimingWheelTask(Runnable runnable, Duration delay) {
        return new SimpleScheduleTask(new Date(System.currentTimeMillis() + delay.toMillis()), runnable, this);
    }
}
