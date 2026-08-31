package com.tbs.web.security.authentication.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.web.security.authentication.AbstractAuthManager;
import com.tbs.web.security.authentication.model.ITokenAuthentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 基于 Spring Security {@link SecurityContextHolder}（ThreadLocal）的认证管理器实现
 * <p>
 * 认证信息随请求线程生命周期存在，适用于传统同步 Servlet 容器。
 * 注意：本实现仅维护「当前线程」的认证上下文，不支持按令牌字符串检索，
 * 故 {@link #clearAuthentication(String)} 与 {@link #getAuthentication(String)} 直接抛 {@link UnsupportedOperationException}。
 * </p>
 *
 * @author abstergo
 * @see AbstractAuthManager 抽象基类
 */
public class SpringSecurityContextHolderAuthManager extends AbstractAuthManager {
    @Override
    public void setAuthentication(ITokenAuthentication authentication) {
        AssertUtil.isTrue(authentication == null, () -> new NullPointerException("authentication cannot be null"));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Override
    public void clearAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Override
    public void clearAuthentication(String token) {
        AssertUtil.throwError(() -> new UnsupportedOperationException(
            "SpringSecurityContextHolderAuthManager does not support clearAuthentication(String token)"));
    }

    @Override
    public ITokenAuthentication getCurrentAuthentication() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        if (!(authentication instanceof ITokenAuthentication)) {
            return null;
        }
        return (ITokenAuthentication)SecurityContextHolder.getContext().getAuthentication();
    }

    @Override
    public ITokenAuthentication getAuthentication(String token) {
        AssertUtil.throwError(() -> new UnsupportedOperationException(
            "SpringSecurityContextHolderAuthManager does not support getAuthentication(String token)"));
        return null;
    }
}
