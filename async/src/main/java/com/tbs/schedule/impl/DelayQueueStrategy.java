package com.tbs.schedule.impl;

import com.tbs.schedule.IScheduleTask;
import com.tbs.schedule.ScheduleStrategy;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * DelayQueue 调度策略
 * <p>
 * 基于 JDK DelayQueue 的纯数据结构实现，不关心线程协调。
 * 适用于任务数量适中、对内存占用敏感的场景。
 * </p>
 *
 * @author abstergo
 */
@Slf4j
public class DelayQueueStrategy implements ScheduleStrategy<IScheduleTask> {

    private final DelayQueue<DelayedTask> delayQueue = new DelayQueue<>();

    @Override
    public void insert(IScheduleTask task) {
        delayQueue.put(new DelayedTask(task));
    }

    @Override
    public Collection<IScheduleTask> pollExpired() {
        try {
            DelayedTask task = delayQueue.poll();
            if (task == null) {
                return List.of();
            }
            return Arrays.asList(task.delegate);
        } catch (Exception e) {
            log.warn("DelayQueue poll error", e);
            return List.of();
        }
    }

    @Override
    public Collection<IScheduleTask> dropAll() {
        List<IScheduleTask> result = new ArrayList<>();
        DelayedTask dt;
        while ((dt = delayQueue.poll()) != null) {
            result.add(dt.delegate);
        }
        return result;
    }

    /**
     * 将 IScheduleTask 适配为 Delayed 接口，供 DelayQueue 使用
     */
    private static class DelayedTask implements Delayed {
        final IScheduleTask delegate;

        DelayedTask(IScheduleTask delegate) {
            this.delegate = delegate;
        }

        @Override
        public long getDelay(@NotNull TimeUnit unit) {
            return unit.convert(delegate.getRemain());
        }

        @Override
        public int compareTo(@NotNull Delayed o) {
            return Long.compare(this.getDelay(TimeUnit.MILLISECONDS), o.getDelay(TimeUnit.MILLISECONDS));
        }
    }
}
