package com.tbs.schedule;

import java.util.Collection;

/**
 * 调度策略接口
 * <p>
 * 定义调度器底层数据结构的操作契约，将调度算法与线程协调逻辑解耦。 不同的实现（时间轮、DelayQueue等）只需关注数据结构本身的插入、轮询和清理。
 * </p>
 *
 * @param <T> 任务类型
 * @author abstergo
 */
public interface ScheduleStrategy<T extends IScheduleTask> {

    /**
     * 将任务插入调度结构
     *
     * @param task 待调度的任务
     */
    void insert(T task);

    /**
     * 轮询到期的任务
     *
     * @return 到期的任务集合，可能为空但不为null
     */
    Collection<T> pollExpired();

    /**
     * 丢弃所有任务并返回，用于调度器销毁时的清理
     *
     * @return 所有未执行的任务
     */
    Collection<T> dropAll();
}
