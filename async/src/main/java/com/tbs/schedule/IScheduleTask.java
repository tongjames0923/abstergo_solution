package com.tbs.schedule;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * 定时任务接口
 *
 * @author tongj
 */
public interface IScheduleTask {
    /**
     * 剩余时间
     *
     * @return
     */
    @NotNull Duration getRemain();

    /**
     * 运行任务
     *
     * @return
     */
    @NotNull Runnable getRunnable();

    /**
     * 获取定时器
     *
     * @return
     */
    @NotNull ISchedule getSchedule();

    /**
     * 是否在计划表中
     *
     * @return
     */
    boolean isPin();

    /**
     * 取消固定在计划表中
     */
    void unPin();

}
