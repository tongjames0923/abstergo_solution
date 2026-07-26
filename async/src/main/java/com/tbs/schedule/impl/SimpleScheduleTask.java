package com.tbs.schedule.impl;

import com.tbs.schedule.ISchedule;
import com.tbs.schedule.IScheduleTask;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Date;

/**
 * 简易的定时任务任务
 *
 * @author tongj
 */
public class SimpleScheduleTask implements IScheduleTask {

    private Date shouldRunTime = null;
    private Runnable runnable = null;
    private ISchedule schedule = null;
    private boolean pin = false;

    @Override
    public  Duration getRemain() {
        if (shouldRunTime == null) {
            return Duration.ZERO;
        }
        long remain = shouldRunTime.getTime() - System.currentTimeMillis();
        return Duration.ofMillis(remain > 0 ? remain : 0);
    }

    @Override
    public  Runnable getRunnable() {
        return runnable;
    }

    @Override
    public  ISchedule getSchedule() {
        return schedule;
    }

    @Override
    public boolean isPin() {
        return pin;
    }

    @Override
    public void unPin() {
        pin = false;
    }

    /**
     * 创建一个定时任务
     *
     * @param shouldRunTime 任务执行时间
     * @param runnable      任务
     * @param schedule      定时器
     */
    public SimpleScheduleTask(Date shouldRunTime, Runnable runnable, ISchedule schedule) {
        this.shouldRunTime = shouldRunTime;
        this.runnable = runnable;
        this.schedule = schedule;
        this.pin = true;
    }

}
