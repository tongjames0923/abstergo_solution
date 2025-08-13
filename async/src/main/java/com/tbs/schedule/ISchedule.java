package com.tbs.schedule;

import org.springframework.beans.factory.DisposableBean;

import java.time.Duration;

/**
 * 定时任务接口
 *
 * @author tongj
 */
public interface ISchedule extends DisposableBean {
    /**
     * 定时执行任务
     *
     * @param runnable
     * @param delay
     * @return
     */
    IScheduleTask pin(Runnable runnable, Duration delay);

    /**
     * 取消定时任务
     *
     * @param task
     */
    void unPin(IScheduleTask task);

    /**
     * 获取定时任务执行错误处理
     *
     * @return
     */
    IScheduleExecuteErrorHandle getExecuteErrorHandle();
}
