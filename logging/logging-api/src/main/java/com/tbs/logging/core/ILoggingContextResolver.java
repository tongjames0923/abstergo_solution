package com.tbs.logging.core;

/**
 * 日志上下文解析门面——日志采集的上下文编排入口
 * <p>
 * 完整链路：定位创建器 → {@link ILoggingContextCreator#create()} 产出快照 →
 * {@link ILoggingContextInterceptor} 管道增强 → 交付采集入口。
 * 全链路无可用创建器或创建均不适用时返回默认空快照，
 * 保证任何执行环境（HTTP、Dubbo、定时任务、裸线程）下日志采集不因上下文缺失而失败。
 * </p>
 *
 * @author abstergo
 * @see ILoggingContextCreator
 * @see ILoggingContextInterceptor
 */
public interface ILoggingContextResolver {

    /**
     * 按来源显式解析上下文
     *
     * @param source 来源标识，见 {@link ILoggingContextCreator#source()}
     * @return 上下文；来源未注册或创建不适用时返回默认空快照，保证不为 null
     */
    ILoggingContext resolve(String source);

    /**
     * 解析当前链路上下文（自动定位）
     * <p>
     * 按创建器优先级升序依次尝试，第一个产出非 empty 快照的创建器生效；
     * 全部不适用时返回默认空快照。
     * </p>
     *
     * @return 上下文，保证不为 null
     */
    ILoggingContext resolveCurrent();
}
