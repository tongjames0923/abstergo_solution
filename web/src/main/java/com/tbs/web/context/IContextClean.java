package com.tbs.web.context;

/**
 * 上下文清理器——请求结束时清理当前线程的请求级上下文
 * <p>
 * 由 {@code RequestContextLifecycleFilter} 在请求生命周期终点（finally）统一调度执行，
 * 每种需要线程级清理的上下文（请求上下文、安全上下文、语言上下文等）提供各自实现，
 * 经 Spring 注册为 Bean 后自动纳入清理管道，无需修改过滤器。
 * </p>
 * <p>
 * <b>可靠性约定</b>：清理动作应可重入、幂等（上下文已清理时再次调用无副作用），
 * 不得抛出异常中断管道——单个清理器失败由调度方捕获降级，不影响其余清理器执行。
 * </p>
 *
 * @author abstergo
 * @see RequestContextHolder
 */
public interface IContextClean {

    /**
     * 清理当前线程的上下文
     * <p>
     * 实现应在请求生命周期终点被调用，清除线程级存储，
     * 防止容器线程复用导致的数据串扰与内存泄漏。
     * </p>
     */
    void clean();

    /**
     * 获取清理顺序
     * <p>
     * 按升序执行，数值越小越先执行。业务语义上下文（依赖基础上下文读取的）
     * 应先于基础上下文清理。
     * </p>
     *
     * @return 清理顺序，默认 0
     */
    default int getOrder() {
        return 0;
    }
}
