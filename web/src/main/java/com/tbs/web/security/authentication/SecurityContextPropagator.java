package com.tbs.web.security.authentication;

import com.tbs.common.context.ContextPropagator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Spring Security 认证上下文传播器
 * <p>
 * 将父线程的 {@link SecurityContext}（包含认证信息、用户详情等）传播到线程池的工作线程中。
 * <p>
 * 解决的问题：
 * Spring Security 默认使用 ThreadLocal 存储 SecurityContext（{@code SecurityContextHolder.MODE_THREADLOCAL}），
 * 线程池的工作线程无法继承父线程的认证信息。这导致异步任务中：
 * <ul>
 *   <li>{@code AuthUtil.getInstance().isUserLoggedIn()} 返回 false</li>
 *   <li>{@code AuthUtil.getInstance().getCurrentUser()} 抛出 NoLoginException</li>
 *   <li>{@code @PreAuthorize} 等安全注解在异步线程中失效</li>
 * </ul>
 * <p>
 * 本传播器通过在提交任务时快照 {@link SecurityContext}，
 * 在工作线程执行前恢复、执行后清理，确保异步任务能正确访问认证信息。
 * <p>
 * 前提条件：
 * 调用方需保证异步任务在请求线程存续期间完成（即请求线程不会在异步任务执行前被清理上下文）。
 *
 * @author abstergo
 * @see ContextPropagator
 * @see SecurityContextHolder
 */
public class SecurityContextPropagator implements ContextPropagator {

    @Override
    public Runnable wrap(Runnable task) {
        // 在调用线程（请求线程）中快照当前 SecurityContext
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        return () -> {
            try {
                // 在工作线程中恢复认证信息
                if (authentication != null) {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
                task.run();
            } finally {
                // 清理，防止线程池复用时残留上一个请求的认证信息
                SecurityContextHolder.clearContext();
            }
        };
    }
}
