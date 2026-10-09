package com.tbs.web.context;

import com.tbs.web.uitl.NetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 请求上下文生成器
 * <p>
 * {@code RequestContextLifecycleFilter} 生成管道的默认实现：获取或创建当前线程的
 * {@link RequestDataContext} 并填充基础运行数据。上下文已存在（如 Dubbo 传播恢复、
 * 更早入口创建）时保留原实例仅刷新基础运行数据，保证 traceId 与调用链一致。
 * </p>
 *
 * @author abstergo
 * @see IContextBuilder
 * @see RequestContextHolder
 */
@Slf4j
@Component
public class RequestContextBuilder implements IContextBuilder {

    /**
     * 生成上下文环境
     * <p>
     * 获取或创建当前线程的请求上下文，并填充客户端 IP、请求方法与请求 URI。
     * </p>
     *
     * @param request 当前 HTTP 请求
     */
    @Override
    public void build(HttpServletRequest request) {
        RequestDataContext context = RequestContextHolder.getOrCreate();
        context.setClientIp(NetUtil.getIp(request));
        context.setRequestMethod(request.getMethod());
        context.setRequestUri(request.getRequestURI());
        log.debug("请求上下文初始化: traceId={}, uri={}", context.getTraceId(), context.getRequestUri());
    }
}
