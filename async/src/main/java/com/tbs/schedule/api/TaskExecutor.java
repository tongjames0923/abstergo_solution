package com.tbs.schedule.api;

/**
 * 任务执行器（SPI）
 * <p>
 * 负责按任务类型解释 {@link TaskPayload} 并执行。调度内核不感知具体执行逻辑，仅按 {@code taskType}
 * 匹配对应的执行器。新增任务类型只需扩展 {@link TaskPayload} 并实现本接口，注册为 Bean 即生效，
 * 无需改动核心逻辑。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface TaskExecutor {

    /**
     * 是否支持指定任务类型
     *
     * @param taskType 任务类型标识
     * @return 支持返回 true
     */
    boolean supports(String taskType);

    /**
     * 执行任务
     *
     * @param submission 任务提交描述
     */
    void execute(TaskSubmission submission);
}
