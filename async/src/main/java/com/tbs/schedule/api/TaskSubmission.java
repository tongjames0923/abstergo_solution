package com.tbs.schedule.api;

import com.tbs.schedule.impl.LocalRunnablePayload;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
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
 * <p>
 * <b>持久化模型：</b>本对象实现 {@link Serializable} 符合框架持久化规范，线格式由
 * {@link com.tbs.schedule.impl.TaskSubmissionTransformer}（common 模块 {@code ITransformer} 数据转换 SPI 的实现）
 * 以 JSON 快照承载（含载荷实现类信息，支持多态还原）。本对象不可变，其能否可靠持久化取决于 {@link #getPayload()}。
 * 若载荷为内存态（如 {@link LocalRunnablePayload} 持有的 {@link Runnable} 闭包），{@link #isPersistable()} 返回 {@code false}，
 * 内核在接入持久化存储时会跳过落盘，进程重启即丢失——适用于缓存 TTL 等无副作用场景。
 * 需要可靠持久化的任务请通过 {@link #of(String, TaskPayload, Instant)} 等入口提交可序列化载荷 + 对应执行器。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class TaskSubmission implements Serializable {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

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
    private final Date triggerAt;

    /**
     * 个性化执行策略覆盖（可空）
     */
    private final ExecutionPolicy policyOverride;

    public TaskSubmission(String taskId, String taskType, TaskPayload payload, Date triggerAt,
        ExecutionPolicy policyOverride) {
        this.taskId = taskId;
        this.taskType = taskType;
        this.payload = payload;
        this.triggerAt = triggerAt;
        this.policyOverride = policyOverride;
    }

    /**
     * 是否可持久化（取决于载荷）
     * <p>
     * 委托 {@link TaskPayload#isPersistable()}。内存态载荷（如持有 {@link Runnable} 闭包的本地任务）返回 {@code false}，
     * 内核在接入持久化存储时会跳过落盘，避免序列化失败或重启后无法还原执行行为。
     * </p>
     *
     * @return 可持久化返回 true
     */
    public boolean isPersistable() {
        return payload != null && payload.isPersistable();
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
    public Date getTriggerAt() {
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
     * <p>
     * <b>非持久化入口：</b>底层载荷为 {@link LocalRunnablePayload}，持有 {@link Runnable} 闭包，无法序列化。
     * 仅适用于进程内临时调度，接入持久化存储时内核跳过落盘，重启即丢失（无副作用场景适用）。 需可靠持久化请改用 {@link #of(String, TaskPayload, Date)}。
     * </p>
     *
     * @param runnable 可运行体
     * @param delay    延迟时长
     * @return 任务提交描述
     */
    public static TaskSubmission local(Runnable runnable, java.time.Duration delay) {
        Date triggerAt = Date.from(Instant.now().plus(delay == null ? java.time.Duration.ZERO : delay));
        return new TaskSubmission(LocalRunnablePayload.nextTaskId(), LocalRunnablePayload.TYPE,
            LocalRunnablePayload.of(runnable), triggerAt, null);
    }

    /**
     * 便捷构建器：本地 Runnable 任务（指定绝对时刻触发）
     * <p>
     * <b>非持久化入口：</b>语义同 {@link #local(Runnable, Duration)}，详见其说明。
     * </p>
     *
     * @param runnable  可运行体
     * @param triggerAt 触发时刻
     * @return 任务提交描述
     */
    public static TaskSubmission localAt(Runnable runnable, Date triggerAt) {
        return new TaskSubmission(LocalRunnablePayload.nextTaskId(), LocalRunnablePayload.TYPE,
            LocalRunnablePayload.of(runnable), triggerAt, null);
    }

    /**
     * 持久化主入口：基于任务类型 + 可序列化载荷构建任务
     * <p>
     * 推荐用于需要可靠持久化的场景。载荷须为纯数据（可序列化 {@link TaskPayload} 实现）， 内核按 {@code taskType} 路由到对应
     * {@link TaskExecutor}，进程重启后可由持久化存储恢复并重新执行。 自动生成任务标识，使用全局/按类型默认执行策略。
     * </p>
     *
     * @param taskType  任务类型（须有注册的执行器支持）
     * @param payload   可序列化任务载荷（不可为 null）
     * @param triggerAt 绝对触发时刻
     * @return 任务提交描述
     */
    public static TaskSubmission of(String taskType, TaskPayload payload, Date triggerAt) {
        return new TaskSubmission(nextTaskId(), taskType, payload, triggerAt, null);
    }

    /**
     * 持久化主入口（带个性化策略）：基于任务类型 + 可序列化载荷构建任务
     *
     * @param taskType       任务类型（须有注册的执行器支持）
     * @param payload        可序列化任务载荷（不可为 null）
     * @param triggerAt      绝对触发时刻
     * @param policyOverride 个性化执行策略（可为 null）
     * @return 任务提交描述
     */
    public static TaskSubmission of(String taskType, TaskPayload payload, Date triggerAt,
        ExecutionPolicy policyOverride) {
        return new TaskSubmission(nextTaskId(), taskType, payload, triggerAt, policyOverride);
    }

    /**
     * 生成全局唯一任务标识（供持久化入口使用）
     *
     * @return 任务标识
     */
    private static String nextTaskId() {
        return LocalRunnablePayload.nextTaskId();
    }
}
