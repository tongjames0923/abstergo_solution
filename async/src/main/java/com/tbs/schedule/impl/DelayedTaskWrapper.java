package com.tbs.schedule.impl;

import com.tbs.schedule.ISchedule;
import com.tbs.schedule.IScheduleTask;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * 延迟任务包装类
 *
 * @author tongj
 */
public class DelayedTaskWrapper implements IScheduleTask, Delayed {

    private IScheduleTask scheduleTask;

    @Override
    public @NotNull Duration getRemain() {
        return scheduleTask.getRemain();
    }

    @Override
    public @NotNull Runnable getRunnable() {
        return scheduleTask.getRunnable();
    }

    @Override
    public @NotNull ISchedule getSchedule() {
        return scheduleTask.getSchedule();
    }

    @Override
    public boolean isPin() {
        return scheduleTask.isPin();
    }

    @Override
    public void unPin() {
        scheduleTask.unPin();
    }

    public DelayedTaskWrapper(IScheduleTask scheduleTask) {
        this.scheduleTask = scheduleTask;
    }

    @Override
    public long getDelay(@NotNull TimeUnit unit) {
        return unit.convert(getRemain());
    }

    @Override
    public int compareTo(@NotNull Delayed o) {
        return o.getDelay(TimeUnit.MILLISECONDS) >= getDelay(TimeUnit.MILLISECONDS) ? 1 : -1;
    }
}
