package com.tbs.logging.core;

import java.util.Optional;

/**
 * 日志上下文创建器——将特定来源的模块上下文适配为 {@link ILoggingContext}
 * <p>
 * 各来源模块（web、Dubbo、定时任务等）提供各自实现，由来源模块的工厂类装配 并注册为 Bean，本契约不规定装配方式。环境适用性判断与创建合一：当前环境 无法提供数据时 {@link #create()} 返回 empty，由
 * {@link ILoggingContextResolver} 决定尝试下一个创建器或降级为空快照，不设独立的"是否支持"探测方法。
 * </p>
 *
 * @author abstergo
 * @see ILoggingContext
 * @see ILoggingContextResolver
 */
public interface ILoggingContextCreator {

    /**
     * 获取来源标识
     * <p>
     * 同一 {@link ILoggingContextResolver} 内不允许重复。
     * </p>
     *
     * @return 来源标识，如 "web" / "dubbo" / "scheduled"
     */
    String source();

    /**
     * 获取自动定位优先级
     * <p>
     * {@link ILoggingContextResolver#resolveCurrent()} 按优先级升序尝试各创建器， 数值越小优先级越高。
     * </p>
     *
     * @return 优先级，默认 0
     */
    default int getOrder() {
        return 0;
    }

    /**
     * 创建日志上下文快照
     * <p>
     * 在采集线程同步调用，实现应即刻完成值捕获， 不得返回依赖后续执行环境的惰性对象。
     * </p>
     *
     * @return 当前环境可提供时的上下文快照；不适用时返回 empty
     */
    Optional<ILoggingContext> create();
}
