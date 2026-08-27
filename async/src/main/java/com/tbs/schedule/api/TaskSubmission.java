package com.tbs.schedule.api;

import com.tbs.schedule.impl.LocalRunnablePayload;

import java.time.Instant;
import java.util.Optional;

/**
 * 任务提交描述（纯数据）
 * <p>
 * 调度框架对外暴露的唯一稳定契约载体，描述"一个待在指定时刻执行一次的任务是什么"。 本对象只承载数据，不含任何生命周期状态或反向引用，状态机演化由内核内部 {@code ScheduledEntry} 维护。
 * </p>
 * <ul>
 *     <li>{@code taskId}：任务唯一标识，用于幂等、查询与取消；</li>
 *     <li>{@code taskType}：任务类型，决定由哪个 {@link TaskExecutor} 解释执行；</li>
 *     <li>{@code payload}：类型化载荷；</li>
 *     <li>{@code triggerAt}：绝对触发时刻（延迟触发由调用方换算为 now + delay）；</li>
 *     <li>{@code policyOverride}：可选的个性化执行策略，覆盖全局/按类型默认策略。</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class TaskSubmission {

    /**
     * 任务唯一标识
     */
    private final String taskId;

    /**
     * 任务类型
     */
    private final String taskType;

    /**
     * 任务类型化载荷
     */
    private final TaskPayload payload;

    /**
     * 绝对触发时刻
     */
    private final Instant triggerAt;

    /**
     * 个性化执行策略覆盖（可空）
     */
    private final ExecutionPolicy policyOverride;

    /**
     * 构造任务提交描述
     *
     * @param taskId         任务唯一标识
     * @param taskType       任务类型
     * @param payload        任务载荷
     * @param triggerAt      绝对触发时刻
     * @param policyOverride 个性化执行策略（可为 null）
     */
    public TaskSubmission(String taskId, String taskType, TaskPayload payload, Instant triggerAt,
        ExecutionPolicy policyOverride) {
        this.taskId = taskId;
        this.taskType = taskType;
        this.payload = payload;
        this.triggerAt = triggerAt;
        this.policyOverride = policyOverride;
    }

    /**
     * 获取任务唯一标识
     *
     * @return 任务标识
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * 获取任务类型
     *
     * @return 任务类型
     */
    public String getTaskType() {
        return taskType;
    }

    /**
     * 获取任务载荷
     *
     * @return 任务载荷
     */
    public TaskPayload getPayload() {
        return payload;
    }

    /**
     * 获取绝对触发时刻
     *
     * @return 触发时刻
     */
    public Instant getTriggerAt() {
        return triggerAt;
    }

    /**
     * 获取个性化执行策略覆盖
     *
     * @return 策略覆盖（可空）
     */
    public Optional<ExecutionPolicy> getPolicyOverride() {
        return Optional.ofNullable(policyOverride);
    }

    /**
     * 便捷构建器：本地 Runnable 任务（延迟触发）
     *
     * @param runnable 可运行体
     * @param delay    延迟时长
     * @return 任务提交描述
     */
    public static TaskSubmission local(Runnable runnable, java.time.Duration delay) {
        Instant triggerAt = Instant.now().plus(delay == null ? java.time.Duration.ZERO : delay);
        return new TaskSubmission(LocalRunnablePayload.nextTaskId(), LocalRunnablePayload.TYPE,
            LocalRunnablePayload.of(runnable), triggerAt, null);
    }

    /**
     * 便捷构建器：本地 Runnable 任务（指定绝对时刻触发）
     *
     * @param runnable  可运行体
     * @param triggerAt 触发时刻
     * @return 任务提交描述
     */
    public static TaskSubmission localAt(Runnable runnable, Instant triggerAt) {
        return new TaskSubmission(LocalRunnablePayload.nextTaskId(), LocalRunnablePayload.TYPE,
            LocalRunnablePayload.of(runnable), triggerAt, null);
    }
}
