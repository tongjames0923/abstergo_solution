package com.tbs.schedule.api;

/**
 * 任务调度句柄
 * <p>
 * 任务被 {@link TaskScheduler#submit(TaskSubmission)} 提交后返回给调用方的唯一把手。
 * 调用方仅能通过本句柄取消任务，无法窥探任务内部状态，从而将"任务数据"与"调度生命周期控制"彻底分离。
 * 取消为软取消语义：不会立即从底层时序结构移除元素（时间轮等结构不便删除），而是在到期轮询时跳过丢弃。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface ScheduleHandle {

    /**
     * 任务唯一标识
     *
     * @return 任务标识
     */
    String getTaskId();

    /**
     * 取消该任务
     * <p>
     * 取消后任务不会被执行；若任务已进入执行阶段则取消无效。
     * 返回是否成功登记取消（任务尚未执行时为 true）。
     * </p>
     *
     * @return 是否成功取消
     */
    boolean cancel();
}
