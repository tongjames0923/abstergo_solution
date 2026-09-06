package com.tbs.web.gate;

import com.tbs.web.context.IContextClean;
import com.tbs.web.context.RequestDataContext;
import com.tbs.web.context.RequestContextHolder;
import com.tbs.web.uitl.NetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 请求上下文生命周期过滤器
 * <p>
 * HTTP 请求生命周期中 {@link RequestDataContext} 的<b>创建与清理统一管理点</b>：
 * <ul>
 *   <li><b>进入时创建并填充</b>：生成链路追踪 ID（traceId）与开始时间，填充客户端
 *       IP、请求方法、请求 URI。上下文在过滤器链最外层就绪，保证后续任何阶段
 *       （Spring Security、AOP 拦截器链、Service 层切面、日志上下文创建器）
 *       读到的都是<b>本请求</b>的数据，不受拦截器执行顺序影响。</li>
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
     * 构造过滤器
     *
     * @param contextCleans 上下文清理器集合，Spring 自动收集全部实现，允许为空
     */
    public RequestContextLifecycleFilter(List<IContextClean> contextCleans) {
        List<IContextClean> sorted = new ArrayList<>(
            contextCleans == null ? new ArrayList<>() : contextCleans);
        sorted.sort(Comparator.comparingInt(IContextClean::getOrder));
        this.contextCleans = sorted;
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
     * 初始化当前请求的请求上下文
     * <p>
     * 上下文已存在（如 Dubbo 传播恢复、更早入口创建）时保留原实例仅刷新
     * 基础运行数据，保证 traceId 与调用链一致；否则创建新上下文。
     * 初始化失败仅记 warn，不阻断请求。
     * </p>
     *
     * @param request 当前 HTTP 请求
     */
    private void initContext(HttpServletRequest request) {
        try {
            RequestDataContext context = RequestContextHolder.get();
            if (context == null) {
                context = RequestDataContext.create();
                RequestContextHolder.set(context);
            }
            context.setClientIp(NetUtil.getIp(request));
            context.setRequestMethod(request.getMethod());
            context.setRequestUri(request.getRequestURI());
            log.debug("请求上下文初始化: traceId={}, uri={}", context.getTraceId(), context.getRequestUri());
        } catch (Exception e) {
            log.warn("请求上下文初始化失败", e);
        }
    }
}
