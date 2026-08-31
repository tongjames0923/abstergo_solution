package com.tbs.schedule.api;

import java.time.Duration;
import java.util.Date;

/**
 * 任务调度门面（对外唯一稳定契约）
 * <p>
 * 调度框架对外的统一入口，所有触发来源（手动调用、事件监听、外部 API 等）都通过本门面提交任务。
 * 门面负责校验、生成调度、将任务交由内核时序引擎；不感知任务如何执行、结果给谁——这些由派发与执行层完成。
 * </p>
 * <p>
 * 所有提交均为"一次性触发"语义：任务到点执行一次后即终结，框架不存在重复定时的概念。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface TaskScheduler {

    /**
     * 提交一个任务
     * <p>
     * 任务将在 {@link TaskSubmission#getTriggerAt()} 指定时刻被触发一次。
     * 返回的 {@link ScheduleHandle} 可用于后续取消。
     * </p>
     *
     * @param submission 任务提交描述，不可为 null
     * @return 任务调度句柄
     */
    ScheduleHandle submit(TaskSubmission submission);

    /**
     * 便捷提交：延迟执行的本地 Runnable 任务（语法糖）
     *
     * @param runnable 可运行体，不可为 null
     * @param delay    延迟时长，为 null 时视为零延迟
     * @return 任务调度句柄
     */
    default ScheduleHandle schedule(Runnable runnable, Duration delay) {
        return submit(TaskSubmission.local(runnable, delay));
    }

    /**
     * 便捷提交：在指定绝对时刻执行的本地 Runnable 任务（语法糖）
     *
     * @param runnable  可运行体，不可为 null
     * @param triggerAt 触发时刻，不可为 null
     * @return 任务调度句柄
     */
    default ScheduleHandle scheduleAt(Runnable runnable, Date triggerAt) {
        return submit(TaskSubmission.localAt(runnable, triggerAt));
    }
}
