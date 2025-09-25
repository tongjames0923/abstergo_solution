package com.tbs.web.security.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.web.security.authentication.center.IAuthenticationCenter;
import com.tbs.web.security.authentication.model.DefaultTokenAuthentication;
import com.tbs.web.security.authentication.model.ITokenAuthentication;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import javax.annotation.Resource;
import java.util.function.Supplier;

public class TokenAuthenticationProvider implements org.springframework.security.authentication.AuthenticationProvider {

    @Resource
    private IAuthenticationCenter authCenter;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        AssertUtil.notNull(authentication, "未获取到有效的Authentication");
        ITokenAuthentication tokenAuthentication = (ITokenAuthentication)authentication;
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
        return authentication == DefaultTokenAuthentication.class;
    }
}
