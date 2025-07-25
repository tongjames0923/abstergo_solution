package com.tbs.schedule;

/**
 * 任务执行错误处理
 * @author tongj
 */
public interface IScheduleExecuteErrorHandle {
    /**
     * 处理任务执行错误
     *
     * @param task
     * @param throwable
     * @param schedule
     */
    void handle(IScheduleTask task, Throwable throwable, ISchedule schedule);
}
