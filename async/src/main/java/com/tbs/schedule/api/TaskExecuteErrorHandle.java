package com.tbs.schedule.api;

/**
 * 任务执行错误处理
 * <p>
 * 任务执行阶段抛出异常时的兜底处理。默认实现仅打日志；业务可注册自定义实现做告警、降级或重投。
 * 一次性触发语义下，失败不会自动重试（重试属执行层补偿，非调度语义）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface TaskExecuteErrorHandle {

    /**
     * 处理任务执行错误
     *
     * @param submission 出错的任务提交描述
     * @param throwable  异常
     */
    void handle(TaskSubmission submission, Throwable throwable);
}
