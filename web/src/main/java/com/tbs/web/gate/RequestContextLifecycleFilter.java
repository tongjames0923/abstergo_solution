package com.tbs.web.gate;

import com.tbs.common.util.base.CollUtil;
import com.tbs.web.context.IContextBuilder;
import com.tbs.web.context.IContextClean;
import com.tbs.web.context.RequestContextHolder;
import com.tbs.web.context.RequestDataContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 请求上下文生命周期过滤器
 * <p>
 * HTTP 请求生命周期中 {@link RequestDataContext} 的<b>创建与清理统一管理点</b>：
 * <ul>
 *   <li><b>进入时创建并填充</b>：经 {@link IContextBuilder} 生成管道按序执行，
 *       由各生成器创建并填充线程级上下文（默认 {@code RequestContextBuilder}
 *       创建 {@link RequestDataContext}，生成链路追踪 ID（traceId）与开始时间，
 *       填充客户端 IP、请求方法、请求 URI）。上下文在过滤器链最外层就绪，
 *       保证后续任何阶段（Spring Security、AOP 拦截器链、Service 层切面、
 *       日志上下文创建器）读到的都是<b>本请求</b>的数据，不受拦截器执行顺序影响。</li>
 *   <li><b>结束时清理</b>：请求返回时（finally）经 {@link IContextClean} 清理管道
 *       统一清理（请求上下文及各扩展实现），防止单个清理器失败影响其余清理器执行、
 *       防止容器线程（线程池复用）残留上一请求数据导致的上下文串台（traceId、URI
 *       错乱）与内存泄漏。</li>
 * </ul>
 * <p>
 * <b>设计说明</b>：
 * <ul>
 *   <li><b>为什么必须在 Filter 层创建</b>：上下文的生命周期是"整个 HTTP 请求"，
 *       而 AOP 拦截器只覆盖 Controller 方法片段且相互执行顺序不确定——若在
 *       拦截器中惰性创建，先执行的采集组件可能读到 null（traceId 缺失）或
 *       残留旧值（串台）。</li>
 *   <li><b>Order 取值</b>：{@code HIGHEST_PRECEDENCE + 5}，比 {@link I18nFilter}
 *       （{@code HIGHEST_PRECEDENCE + 10}）更早进入过滤器链、其 finally 更晚执行，
 *       保证清理发生在包括语言上下文在内的全部下游阶段完成之后。</li>
 *   <li><b>与 Dubbo 链路的关系</b>：Dubbo 消费/提供侧的上下文传播与恢复由
 *       {@code DubboRequestContextFilter} 负责，本过滤器仅管理 HTTP 入口。</li>
 *   <li><b>命名说明</b>：类名不使用 {@code RequestContextFilter}，避免与
 *       Spring Boot {@code WebMvcAutoConfiguration} 内置注册的同名 Bean 冲突
 *       （Bean 定义覆盖默认关闭）。</li>
 * </ul>
 *
 * @author abstergo
 * @see OncePerRequestFilter
 * @see IContextBuilder
 * @see IContextClean
 * @see RequestDataContext
 * @see RequestContextHolder
 * @see I18nFilter
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class RequestContextLifecycleFilter extends OncePerRequestFilter {

    /**
     * 上下文清理管道（已按 {@link IContextClean#getOrder()} 升序排列）
     */
    private final List<IContextClean> contextCleans;

    /**
     * 上下文生成管道（已按 {@link IContextBuilder#getOrder()} 升序排列）
     */
    private final List<IContextBuilder> contextBuilders;

    /**
     * 构造过滤器
     * <p>
     * 两条管道均为构造时一次性收集并排序，避免每请求重复执行
     * Bean 查找与排序的开销。
     * </p>
     *
     * @param contextCleans  上下文清理器集合，Spring 自动收集全部实现，允许为空
     * @param contextBuilders 上下文生成器集合，Spring 自动收集全部实现，允许为空
     */
    public RequestContextLifecycleFilter(List<IContextClean> contextCleans, List<IContextBuilder> contextBuilders) {
        List<IContextClean> sorted = new ArrayList<>(
            contextCleans == null ? new ArrayList<>() : contextCleans);
        sorted.sort(Comparator.comparingInt(IContextClean::getOrder));
        this.contextCleans = sorted;
        this.contextBuilders = new ArrayList<>(contextBuilders == null ? new ArrayList<>() : contextBuilders);
        this.contextBuilders.sort(Comparator.comparingInt(IContextBuilder::getOrder));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        try {
            initContext(request);
            filterChain.doFilter(request, response);
        } finally {
            cleanContexts();
        }
    }

    /**
     * 执行上下文清理管道
     * <p>
     * 按顺序依次执行全部清理器；单个清理器异常仅记 warn 并继续，
     * 保证其余上下文仍被清理。
     * </p>
     */
    private void cleanContexts() {
        for (IContextClean contextClean : contextCleans) {
            try {
                contextClean.clean();
            } catch (Exception e) {
                log.warn("上下文清理器执行失败: {}", contextClean.getClass().getName(), e);
            }
        }
    }

    /**
     * 执行上下文生成管道
     * <p>
     * 按顺序依次执行全部生成器；单个生成器异常仅记 warn 并继续，
     * 保证其余上下文仍被初始化，且不阻断请求。
     * </p>
     *
     * @param request 当前 HTTP 请求
     */
    private void initContext(HttpServletRequest request) {
        if (CollUtil.isEmpty(contextBuilders)) {
            log.debug("请求上下文初始化: 无上下文生成器");
            return;
        }
        for (IContextBuilder contextBuilder : contextBuilders) {
            try {
                contextBuilder.build(request);
            } catch (Exception e) {
                log.warn("上下文生成器执行失败: {}", contextBuilder.getClass().getName(), e);
            }
        }
    }
}
