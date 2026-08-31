package com.tbs.schedule.impl;

import com.tbs.schedule.api.ScheduleTrigger;
import com.tbs.schedule.api.Tickable;

/**
 * 空触发器
 * <p>
 * 用于外部调度模式：内核不自启轮询循环，由外部调度器（任意实现）直接调用
 * {@link Tickable#tick()}。本触发器所有方法均为空实现，不持有任何线程或信号量。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class NoopScheduleTrigger implements ScheduleTrigger {

    @Override
    public void start(Tickable tickable) {
        /* 外部调度模式不启动内部循环，tick 由调用方驱动 */
    }

    @Override
    public void stop() {
        /* 无内部资源需释放 */
    }

    @Override
    public void wakeup() {
        /* 无内部阻塞等待，无需唤醒 */
    }
}
