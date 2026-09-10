package com.tbs.web.context;

import org.springframework.core.Ordered;

import javax.servlet.http.HttpServletRequest;

/**
 * 上下文环境生成器
 * <p>
 * 由 {@code RequestContextLifecycleFilter} 在请求生命周期起点统一调度执行，
 * 每种需要在请求入口初始化的线程级上下文（请求上下文、安全上下文等）提供各自实现，
 * 经 Spring 注册为 Bean 后自动纳入生成管道，无需修改过滤器。
 * </p>
 * <p>
 * <b>可靠性约定</b>：与 {@link IContextClean} 对称——
 * <ul>
 *   <li>build 应幂等、可重入：上下文已存在（如 Dubbo 传播恢复、更早入口创建）时
 *       复用刷新，不应盲目重建导致 traceId 断链；</li>
 *   <li>不应抛出异常中断管道——单个生成器失败由调度方捕获降级（记 warn），
 *       不影响其余生成器执行与请求处理。</li>
 * </ul>
 * </p>
 *
 * @author abstergo
 * @see IContextClean
 * @see com.tbs.web.gate.RequestContextLifecycleFilter
 */
public interface IContextBuilder extends Ordered {

    /**
     * 生成上下文环境
     * <p>
     * 在请求进入时被调用，创建或刷新当前线程的上下文。
     * </p>
     *
     * @param request 当前 HTTP 请求
     */
    void build(HttpServletRequest request);

    /**
     * 获取生成顺序
     * <p>
     * 按升序执行，数值越小越先执行。依赖其他上下文已就绪的生成器
     * 应返回更大的值。
     * </p>
     *
     * @return 生成顺序，默认 0
     */
    @Override
    default int getOrder() {
        return 0;
    }
}
