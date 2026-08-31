package com.tbs.schedule.xxl;

import com.tbs.common.delegate.IBackgroundJob;
import com.tbs.schedule.api.ScheduleTrigger;
import com.tbs.schedule.api.Tickable;

/**
 * xxl-job 外部调度触发器
 * <p>
 * 对接 xxl-job 调度框架的 {@link IBackgroundJob} 适配器：以外部调度模式驱动内核运行， 内核不自启轮询循环（区别于 {@code LocalThreadScheduleTrigger}）。xxl-job
 * 调度中心按 cron 触发本类的 {@link #execute()} 回调，由回调单次驱动 {@link Tickable#tick()}。
 * </p>
 * <p>
 * 因驱动权完全交由 xxl-job 框架，本触发器的 {@code start}/{@code stop}/{@code wakeup} 均不持有线程或信号量，为空实现：{@code start} 仅缓存被驱动的内核视图以备
 * {@code execute} 调用， {@code stop}/{@code wakeup} 无内部资源需释放或阻塞等待。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class XxlJobScheduleTrigger implements IBackgroundJob, ScheduleTrigger {

    /**
     * 被驱动的内核视图（start 时注入，由 execute 回调消费）
     */
    private Tickable tickable;

    /**
     * 启动触发器
     * <p>
     * 外部调度模式下不启动内部循环，仅缓存被驱动的内核视图，tick 由 xxl-job 框架经 {@link #execute()} 驱动。
     * </p>
     *
     * @param tickable 可被单次驱动的内核视图，不可为 null
     */
    @Override
    public void start(Tickable tickable) {
        this.tickable = tickable;
    }

    /**
     * 停止触发器
     * <p>
     * 无内部资源（线程/信号量）需释放，空实现。内核停机由 xxl-job 调度中心取消任务衔接。
     * </p>
     */
    @Override
    public void stop() {
        /* 外部调度模式无内部资源需释放 */
    }

    /**
     * 唤醒触发器
     * <p>
     * 无内部阻塞等待，无需主动唤醒，空实现。驱动频率完全由 xxl-job 调度中心的 cron 决定。
     * </p>
     */
    @Override
    public void wakeup() {
        /* 外部调度模式无内部阻塞等待，无需唤醒 */
    }

    /**
     * 任务名称
     *
     * @return 后台任务注册名
     */
    @Override
    public String name() {
        return "xxl-job-trigger";
    }

    /**
     * 任务描述
     *
     * @return 后台任务描述
     */
    @Override
    public String description() {
        return "xxl-job 外部调度驱动";
    }

    /**
     * 执行方法（xxl-job 调度回调入口）
     * <p>
     * 由 xxl-job 调度中心按 cron 触发，单次驱动内核执行一轮到期任务派发。
     * </p>
     */
    @Override
    public void execute() {
        tickable.tick();
    }
}
