package com.tbs.web.security.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.i18n.I18nUtil;
import com.tbs.web.security.authentication.center.IAuthenticationCenter;
import com.tbs.web.security.authentication.model.DefaultTokenAuthentication;
import com.tbs.web.security.authentication.model.ITokenAuthentication;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import jakarta.annotation.Resource;
import java.util.function.Supplier;
import com.tbs.web.constant.WebI18nConstant;

/**
 * 令牌认证提供者
 * <p>
 * 接入 Spring Security 认证链：将入参 {@link ITokenAuthentication} 委托给 {@link IAuthenticationCenter} 完成校验。
 * 若认证对象已携带认证异常（预处理阶段产生的失败原因），直接以其作为异常抛出，避免重复校验。
 * </p>
 *
 * @see org.springframework.security.authentication.AuthenticationProvider Spring Security 认证提供者契约
 */
public class TokenAuthenticationProvider implements org.springframework.security.authentication.AuthenticationProvider {

    @Resource
    private IAuthenticationCenter authCenter;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        AssertUtil.notNull(authentication, I18nUtil.get(com.tbs.web.constant.WebI18nConstant.AUTH_INVALID_AUTHENTICATION));
        ITokenAuthentication tokenAuthentication = (ITokenAuthentication)authentication;
        // 已存在预处理异常时直接复用，不再走认证中心校验
        AssertUtil.isTrue(tokenAuthentication.getAuthException() != null, new Supplier<RuntimeException>() {
            @Override
            public RuntimeException get() {
                return tokenAuthentication.getAuthException();
            }
        });

        return authCenter.authenticate(tokenAuthentication);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        // 仅处理本系统令牌认证类型，其他认证（如表单）交由 Spring Security 其它 Provider
        return authentication == DefaultTokenAuthentication.class;
    }
}