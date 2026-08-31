package com.tbs.schedule.api;

/**
 * 可被单次驱动的内核视图（触发契约）
 * <p>
 * 内核对外暴露的最小驱动能力：一次 {@code tick()} 即"取出到期任务并派发"，不依赖线程上下文、
 * 循环标志或信号量。触发器只认此契约，从而与具体内核实现解耦，保持 api 包自洽、无反向依赖。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface Tickable {

    /**
     * 单次驱动：轮询到期任务并派发
     * <p>
     * 设计为可独立、可重复、可并发被任意触发方调用；内部仅做状态无关的一次性取出与派发。
     * </p>
     */
    void tick();
}
