package com.tbs.schedule.api;

/**
 * 任务生命周期监听器（SPI）
 * <p>
 * 观测任务在整个管道中的状态流转，用于埋点、审计、metrics 与告警。监听器异常被内核隔离，
 * 不影响主流程。所有方法均有默认空实现，监听方按需覆写。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface LifecycleListener {

    /**
     * 任务已提交并进入调度
     *
     * @param submission 任务
     */
    default void onSubmitted(TaskSubmission submission) {
    }

    /**
     * 任务已到期并派发
     *
     * @param submission 任务
     */
    default void onDispatched(TaskSubmission submission) {
    }

    /**
     * 任务开始执行
     *
     * @param submission 任务
     */
    default void onRunning(TaskSubmission submission) {
    }

    /**
     * 任务执行成功
     *
     * @param submission 任务
     */
    default void onSucceeded(TaskSubmission submission) {
    }

    /**
     * 任务执行失败
     *
     * @param submission 任务
     * @param throwable  异常
     */
    default void onFailed(TaskSubmission submission, Throwable throwable) {
    }

    /**
     * 任务被取消
     *
     * @param submission 任务
     */
    default void onCancelled(TaskSubmission submission) {
    }
}
