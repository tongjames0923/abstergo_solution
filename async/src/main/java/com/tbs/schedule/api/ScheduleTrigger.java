package com.tbs.schedule.api;

/**
 * 调度触发器（驱动内核运行的 SPI 契约）
 * <p>
 * 把"何时驱动内核运行"与"内核做什么"解耦：内核只暴露无状态 {@code tick()}，由本触发器决定
 * 以何种方式周期性/事件式地调用它。计时引擎阻塞等待、线程循环、xxl-job 定时调用等差异
 * 全部收口到具体触发器实现，内核不感知任何线程上下文或循环状态。
 * </p>
 * <p>
 * 注意：本接口是内核内部的驱动契约，非对外业务门面（对外稳定契约仍为 {@link TaskScheduler}）。
     * 触发器只依赖 {@link Tickable} 驱动契约，不耦合具体内核实现，从而保持 api 包自洽。
     * </p>
     *
     * @author abstergo
     * @since 1.0.0
 */
public interface ScheduleTrigger {

    /**
     * 启动触发器
     * <p>
     * 触发方据此开始周期性/阻塞式地驱动 {@code tickable.tick()}。内置线程模式下启动常驻轮询循环；
     * 外部调度模式下可空实现，由外部定时调用 {@code tick()}。
     * </p>
     *
     * @param tickable 可被单次驱动的内核视图，不可为 null
     */
    void start(Tickable tickable);

    /**
     * 停止触发器
     * <p>
     * 触发方据此终止驱动（如退出本地轮询循环、释放阻塞）。与内核停机流程衔接，
     * 不影响内核自身的到期任务强刷逻辑。
     * </p>
     */
    void stop();

    /**
     * 唤醒触发器
     * <p>
     * 新任务提交时通知触发器立即驱动一次 {@code tick()}，避免等待下一个轮询周期。
     * 本地线程模式下对应信号量释放；外部调度模式下可为空实现。
     * </p>
     */
    void wakeup();
}
