package com.tbs.web.logging;

import com.tbs.logging.core.DefaultLoggingContext;
import com.tbs.logging.core.ILoggingContext;
import com.tbs.logging.core.ILoggingContextCreator;
import com.tbs.web.context.RequestDataContext;
import com.tbs.web.context.RequestContextHolder;
import com.tbs.web.uitl.AuthUtil;
import com.tbs.web.uitl.NetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * Web 来源日志上下文创建器
 * <p>
 * 将 web 模块的请求上下文（{@link RequestDataContext}）适配为日志上下文快照：
 * HTTP 链路优先读取请求上下文数据（链路追踪 ID、IP、请求方法、URI 等），
 * 缺失字段从当前 {@link HttpServletRequest} 兜底补齐；Dubbo 传播链路
 * （有请求上下文、无 HTTP 请求对象）仅读取请求上下文数据；
 * 两者均不存在（定时任务、裸线程）时返回 empty，由解析门面降级。
 * </p>
 * <p>
 * 操作用户来自认证工具（登录用户名），未登录时降级为固定标识。
 * </p>
 *
 * @author abstergo
 * @see WebLoggingContextFactory
 * @see ILoggingContextCreator
 */
@Slf4j
public class WebLoggingContextCreator implements ILoggingContextCreator {

    /**
     * Web 来源标识
     */
    public static final String SOURCE = "web";

    /**
     * 未登录用户的操作用户兜底标识
     */
    private static final String NULL_USERNAME = "NULL USERNAME";

    @Override
    public String source() {
        return SOURCE;
    }

    @Override
    public Optional<ILoggingContext> create() {
        RequestDataContext requestContext = RequestContextHolder.get();
        HttpServletRequest request = currentHttpRequest();
        if (requestContext == null && request == null) {
            return Optional.empty();
        }
        long startTime =
            requestContext != null && requestContext.getStartTime() > 0 ? requestContext.getStartTime()
                : System.currentTimeMillis();
        DefaultLoggingContext context = new DefaultLoggingContext(SOURCE, startTime);
        if (requestContext != null) {
            context.setTraceId(requestContext.getTraceId());
            context.setOperateIp(requestContext.getClientIp());
            context.setRequestUrl(requestContext.getRequestUri());
            context.setRequestMethod(requestContext.getRequestMethod());
        }
        fillFromHttpRequest(context, request);
        fillOperateUser(context);
        return Optional.of(context);
    }

    /**
     * 从当前 HTTP 请求兜底补齐缺失字段
     *
     * @param context 日志上下文快照
     * @param request 当前 HTTP 请求，允许为 null（Dubbo 传播等无 HTTP 请求的链路）
     */
    private void fillFromHttpRequest(DefaultLoggingContext context, HttpServletRequest request) {
        if (request == null) {
            return;
        }
        if (!context.getOperateIp().isPresent()) {
            context.setOperateIp(NetUtil.getIp(request));
        }
        if (!context.getRequestUrl().isPresent()) {
            context.setRequestUrl(request.getRequestURI());
        }
        if (!context.getRequestMethod().isPresent()) {
            context.setRequestMethod(request.getMethod());
        }
    }

    /**
     * 填充操作用户
     * <p>
     * 登录态读取失败（认证组件未就绪等）降级为兜底标识，不阻断上下文创建。
     * </p>
     *
     * @param context 日志上下文快照
     */
    private void fillOperateUser(DefaultLoggingContext context) {
        try {
            AuthUtil authUtil = AuthUtil.getInstance();
            if (authUtil != null && authUtil.isUserLoggedIn()) {
                context.setOperateUser(authUtil.getCurrentUser().getUsername());
                return;
            }
        } catch (Exception e) {
            log.debug("日志上下文读取登录用户失败, 降级为匿名标识", e);
        }
        context.setOperateUser(NULL_USERNAME);
    }

    /**
     * 获取当前线程的 HTTP 请求对象
     *
     * @return 当前 HTTP 请求；非 HTTP 链路（Dubbo、定时任务等）为 null
     */
    private HttpServletRequest currentHttpRequest() {
        RequestAttributes attributes =
            org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes)attributes).getRequest();
        }
        return null;
    }
}
