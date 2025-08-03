package com.tbs.schedule;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @author abstergo
 */
@Slf4j
public abstract class AbstractDelayQueueAsyncBackgroundSchedule<TASK extends Delayed & IScheduleTask>
    extends AbstractAsyncBackgroundSchedule<TASK> {
    public AbstractDelayQueueAsyncBackgroundSchedule(IScheduleExecuteErrorHandle executeErrorHandle, Long pollTime,
        TimeUnit pollTimeUnit) {
        super(executeErrorHandle, pollTime, pollTimeUnit);
    }

    private DelayQueue<TASK> delayQueue = new DelayQueue<>();

    @Override
    protected void submitTask(TASK task) {
        delayQueue.put(task);
    }

    @Override
    protected Collection<TASK> pollTask(long t, TimeUnit unit) {
        try {
            return Arrays.asList(delayQueue.poll(t, unit));
        } catch (InterruptedException e) {
            log.warn("poll task error", e);
            return null;
        }
    }
}
