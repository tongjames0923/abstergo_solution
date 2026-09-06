package com.tbs.logging.core;

/**
 * 日志上下文拦截器——上下文创建后的增强管道
 * <p>
 * 在 {@link ILoggingContextCreator#create()} 产出快照后、交付采集入口前，
 * 按优先级依次执行，用于补默认值、数据脱敏、埋点标记等通用增强。
 * 编排层对单个拦截器的异常做捕获降级（跳过该拦截器继续管道），
 * 实现不应依赖异常传递语义。
 * </p>
 *
 * @author abstergo
 * @see ILoggingContext
 * @see ILoggingContextResolver
 */
public interface ILoggingContextInterceptor {

    /**
     * 获取执行优先级
     * <p>
     * 按升序执行，数值越小越先执行。
     * </p>
     *
     * @return 优先级，默认 0
     */
    default int getOrder() {
        return 0;
    }

    /**
     * 增强上下文
     *
     * @param context 待增强的日志上下文
     */
    void intercept(ILoggingContext context);
}
