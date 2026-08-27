package com.tbs.schedule;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.lock.util.LockUtil;
import com.tbs.schedule.api.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 调度内核
 * <p>
 * 仅负责"按时把到期任务交给派发器"：底层时序引擎驱动、任务生命周期状态维护、优雅停机。 内核不感知任务如何执行（执行职责交给 {@link TaskDispatcher}），也不感知"由谁、何时驱动自己运行"——
 * 单次驱动能力收口为无状态 {@link #tick()}，具体的线程循环/信号量阻塞/xxl-job 定时调用由 {@link ScheduleTrigger} 决定。任务模型与状态被收口到内核内部
 * {@link ScheduledEntry}，对外只暴露 {@link TaskScheduler} 门面与取消句柄，彻底消除旧 {@code IScheduleTask} 上数据/状态/反向引用混杂的问题。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
public class SchedulerCore implements TaskScheduler, Tickable {

    /**
     * 销毁时是否强制立即执行所有未完成任务
     */
    @Getter
    @Setter
    private boolean executeAllOnDestroy = false;

    /**
     * 底层时序策略（时间轮 / DelayQueue）
     */
    private final ScheduleStrategy strategy;

    /**
     * 派发器（执行外移）
     */
    private final TaskDispatcher dispatcher;

    /**
     * 任务存储（可靠性扩展点）
     */
    private final TaskStore taskStore;

    /**
     * 生命周期监听器
     */
    private final List<LifecycleListener> listeners;

    /**
     * 触发器（驱动契约，解耦"何时运行"与"内核做什么"）
     */
    private final ScheduleTrigger trigger;

    /**
     * 构造调度内核
     *
     * @param strategy   时序策略
     * @param dispatcher 派发器
     * @param taskStore  任务存储
     * @param listeners  生命周期监听器
     * @param trigger    触发器
     */
    public SchedulerCore(ScheduleStrategy strategy, TaskDispatcher dispatcher, TaskStore taskStore,
        List<LifecycleListener> listeners, ScheduleTrigger trigger) {
        this.strategy = strategy;
        this.dispatcher = dispatcher;
        this.taskStore = taskStore;
        this.listeners = listeners == null ? List.of() : listeners;
        this.trigger = trigger;
    }

    @Override
    public ScheduleHandle submit(TaskSubmission submission) {
        AssertUtil.notNull(submission, "schedule.submission.null");
        AssertUtil.notNull(submission.getPayload(), "schedule.submission.payload.null");
        ScheduledEntry entry = new ScheduledEntry(submission);
        taskStore.save(submission);
        LockUtil.getInstance().lockThen(this, () -> strategy.insert(entry));
        log.info("任务已提交: taskId={}, taskType={}, triggerAt={}", submission.getTaskId(), submission.getTaskType(),
            submission.getTriggerAt());
        trigger.wakeup();
        listeners.forEach(l -> l.onSubmitted(submission));
        return entry;
    }

    @Override
    public void tick() {
        Collection<ScheduledEntry> dueEntries = pollDue();
        if (CollUtil.isEmpty(dueEntries)) {
            return;
        }
        log.debug("调度内核 tick: 到期任务数={}", dueEntries.size());
        dueEntries.forEach(this::dispatchEntry);
    }

    /**
     * 惰性等待并取出到期任务
     *
     * @return 到期任务集合
     */
    private Collection<ScheduledEntry> pollDue() {
        final var holder = new Object() {
            Collection<ScheduledEntry> tasks;
        };
        LockUtil.getInstance().lockThen(this, () -> holder.tasks =
            strategy.pollExpired().stream().map(ScheduledEntry.class::cast).collect(Collectors.toList()));
        return holder.tasks;
    }

    /**
     * 派发单个到期任务
     *
     * @param entry 到期任务
     */
    private void dispatchEntry(ScheduledEntry entry) {
        if (!entry.tryCancelCheckAndMarkRunning()) {
            taskStore.remove(entry.getSubmission().getTaskId());
            log.warn("任务已取消，跳过派发: taskId={}", entry.getSubmission().getTaskId());
            listeners.forEach(l -> l.onCancelled(entry.getSubmission()));
            return;
        }
        TaskSubmission submission = entry.getSubmission();
        taskStore.remove(submission.getTaskId());
        log.info("到期任务派发执行: taskId={}, taskType={}", submission.getTaskId(), submission.getTaskType());
        dispatcher.dispatch(submission);
    }

    /**
     * 优雅停机：停止触发循环，按配置决定是否强刷未完成任务
     *
     * @throws Exception 异常
     */
    public void destroy() throws Exception {
        trigger.stop();
        log.info("调度内核已停止触发循环");
        if (executeAllOnDestroy) {
            log.warn("调度内核销毁中，所有未完成任务将提前派发执行");
            Collection<ScheduledEntry> all =
                strategy.dropAll().stream().map(ScheduledEntry.class::cast).collect(Collectors.toList());
            log.warn("调度内核销毁强刷未完成任务数={}", all.size());
            all.forEach(this::dispatchEntry);
        }
    }

    /**
     * 内核内部任务条目
     * <p>
     * 封装提交描述 + 生命周期状态（取消/运行标记），任务模型与状态收口于此，不污染对外契约。
     * </p>
     */
    static final class ScheduledEntry implements ScheduleHandle, ScheduleStrategy.Schedulable {

        @Getter
        private final TaskSubmission submission;

        private volatile boolean cancelled = false;

        private volatile boolean running = false;

        ScheduledEntry(TaskSubmission submission) {
            this.submission = submission;
        }

        @Override
        public String getTaskId() {
            return submission.getTaskId();
        }

        @Override
        public boolean cancel() {
            if (running) {
                return false;
            }
            cancelled = true;
            return true;
        }

        /**
         * 取消检查并标记运行（软取消语义：已取消则跳过，并发安全）
         *
         * @return 可继续派发返回 true
         */
        boolean tryCancelCheckAndMarkRunning() {
            if (cancelled) {
                return false;
            }
            running = true;
            return true;
        }

        @Override
        public Instant getTriggerAt() {
            return submission.getTriggerAt();
        }

        @Override
        public Duration getRemain() {
            long remain = Duration.between(Instant.now(), submission.getTriggerAt()).toMillis();
            return Duration.ofMillis(remain > 0 ? remain : 0);
        }
    }
}
