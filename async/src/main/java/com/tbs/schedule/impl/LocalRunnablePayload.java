package com.tbs.schedule.impl;

import com.tbs.schedule.api.TaskPayload;

import java.util.UUID;

/**
 * 本地 Runnable 任务载荷
 * <p>
 * 将 {@link Runnable} 适配为 {@link TaskPayload}，作为"本地直接执行"这一默认任务类型（{@code type = local}）的载体。
 * 既支撑本地代码直接提交 {@link Runnable} 的轻量习惯，又使任务体在类型体系上与其它 Payload 保持一致。
 * </p>
 * <p>
 * <b>注意：本载荷为内存态，不可持久化。</b> {@link Runnable} 闭包无法序列化，进程重启后无法还原执行行为，
 * 因此 {@link #isPersistable()} 返回 {@code false}，接入持久化存储时内核会跳过其落盘（重启即丢失，无副作用场景适用）。
 * 需要可靠持久化的任务请改用可序列化 {@link TaskPayload} 实现 + 对应 {@link com.tbs.schedule.api.TaskExecutor}。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class LocalRunnablePayload implements TaskPayload {

    /**
     * 本地任务类型标识
     */
    public static final String TYPE = "local";

    private final Runnable runnable;

    /**
     * 构造本地 Runnable 载荷
     *
     * @param runnable 待执行的可运行体，不可为 null
     */
    public LocalRunnablePayload(Runnable runnable) {
        this.runnable = runnable;
    }

    /**
     * 获取待执行的可运行体
     *
     * @return 可运行体
     */
    public Runnable getRunnable() {
        return runnable;
    }

    /**
     * 内存态载荷：不可持久化（{@link Runnable} 闭包无法序列化，重启后无法还原）
     *
     * @return 始终返回 false
     */
    @Override
    public boolean isPersistable() {
        return false;
    }

    /**
     * 便捷工厂：将 Runnable 包装为本地任务载荷
     *
     * @param runnable 可运行体
     * @return 本地任务载荷
     */
    public static LocalRunnablePayload of(Runnable runnable) {
        return new LocalRunnablePayload(runnable);
    }

    /**
     * 便捷工厂：生成随机任务标识
     *
     * @return 全局唯一任务标识
     */
    public static String nextTaskId() {
        return "task-" + UUID.randomUUID().toString().replace("-", "");
    }
}
