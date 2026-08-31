package com.tbs.schedule.impl;

import com.tbs.schedule.api.ScheduleStrategy;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 延迟队列调度策略
 * <p>
 * 基于 {@link DelayQueue} 的纯数据结构实现，不关心线程协调与任务创建。适配底层元素为
 * {@link ScheduleStrategy.Schedulable}，调度内核内部 {@code ScheduledEntry} 实现该契约。
 * </p>
 * <p>
 * 修复项（相对旧实现）：
 * <ul>
 *     <li>{@code getDelay} 统一以毫秒换算，避免默认纳秒导致任务几乎立即到期、延迟失效；</li>
 *     <li>{@code pollExpired} 使用 {@code drainTo} 取尽到期任务，避免批量 TTL 过期严重滞后。</li>
 * </ul>
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class DelayQueueStrategy implements ScheduleStrategy {

    private final DelayQueue<DelayTask> queue = new DelayQueue<>();

    @Override
    public void insert(Schedulable task) {
        queue.add(new DelayTask(task));
    }

    @Override
    public Collection<Schedulable> pollExpired() {
        Collection<DelayTask> expired = new ArrayList<>();
        queue.drainTo(expired);
        // DelayTask 仅包裹 Schedulable，回流时还原为委托的 Schedulable 元素
        return expired.stream().map(t -> t.delegate).collect(Collectors.toList());
    }

    @Override
    public Collection<Schedulable> dropAll() {
        Collection<DelayTask> all = new ArrayList<>();
        queue.drainTo(all);
        // DelayTask 仅包裹 Schedulable，回流时还原为委托的 Schedulable 元素
        return all.stream().map(t -> t.delegate).collect(Collectors.toList());
    }

    /**
     * 适配 DelayQueue 的元素包装
     */
    static final class DelayTask implements Delayed {

        private final Schedulable delegate;

        DelayTask(Schedulable delegate) {
            this.delegate = delegate;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            Duration remain = delegate.getRemain();
            return unit.convert(remain.toMillis(), TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            return Long.compare(this.getDelay(TimeUnit.MILLISECONDS), o.getDelay(TimeUnit.MILLISECONDS));
        }

        @Override
        public String toString() {
            return delegate.toString();
        }
    }
}
