package com.tbs.schedule.api;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;

/**
 * 调度策略接口（时序引擎 SPI）
 * <p>
 * 定义调度器底层数据结构的操作契约，将调度算法（时间轮 / DelayQueue 等）与线程协调逻辑解耦。
 * 不同的实现只需关注数据结构本身的插入、轮询和清理，不感知线程、错误处理与生命周期。
 * </p>
 * <p>
 * 时序元素统一实现内部 {@link Schedulable} 契约，而非对外 {@link TaskSubmission}，
 * 从而把"任务数据"与"底层调度所需的时间视图"分离，避免旧 {@code IScheduleTask} 反向引用调度器的混乱。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface ScheduleStrategy {

    /**
     * 可调度元素（底层时序结构关心的最小契约）
     */
    interface Schedulable {

        /**
         * 获取触发时刻
         *
         * @return 触发时刻
         */
        Date getTriggerAt();

        /**
         * 获取剩余延迟
         *
         * @return 剩余时长
         */
        Duration getRemain();
    }

    /**
     * 将任务插入调度结构
     *
     * @param task 待调度的任务
     */
    void insert(Schedulable task);

    /**
     * 轮询到期的任务
     *
     * @return 到期的任务集合，可能为空但不为 null
     */
    Collection<Schedulable> pollExpired();

    /**
     * 丢弃所有任务并返回，用于调度器销毁时的清理
     *
     * @return 所有未执行的任务
     */
    Collection<Schedulable> dropAll();
}
