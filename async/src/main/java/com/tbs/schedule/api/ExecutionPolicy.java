package com.tbs.schedule.api;

import java.time.Duration;

/**
 * 任务执行策略
 * <p>
 * 描述任务"如何被执行"的可配置策略，与调度时序解耦。策略内容包含执行超时、并发隔离分组、
 * 失败处理器与优先级。策略解析遵循三级覆盖（单任务提交指定 &gt; 按任务类型配置 &gt; 全局默认），
 * 解析模式与 cache 包 {@code ICacheTtlPolicy} 一致。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface ExecutionPolicy {

    /**
     * 执行超时时间，超时由执行线程池侧中断或标记失败
     *
     * @return 超时时长（null 表示不限制）
     */
    Duration getTimeout();

    /**
     * 并发隔离分组标识
     * <p>
     * 相同分组的任务共享同一执行线程池，不同分组相互隔离，避免慢任务类型拖垮其它类型。
     * </p>
     *
     * @return 隔离分组标识（null 表示使用默认分组）
     */
    String getIsolationGroup();

    /**
     * 执行优先级（仅影响同批到期任务的执行先后顺序，不影响触发精度）
     *
     * @return 优先级数值，越大越优先
     */
    int getPriority();

    /**
     * 任务执行失败处理器
     *
     * @return 失败处理器
     */
    TaskExecuteErrorHandle getErrorHandle();
}
