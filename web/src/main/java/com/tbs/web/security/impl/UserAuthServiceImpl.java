package com.tbs.web.security.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.BeanUtil;
import com.tbs.common.util.StrUtil;
import com.tbs.web.exception.NotFoundTokenException;
import com.tbs.web.exception.SimpleAuthException;
import com.tbs.web.security.ITokenAuthentication;
import com.tbs.web.security.IUserAuthService;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.annotation.Resource;

/**
 * 默认用户认证服务实现
 *
 * @author jiangnan
 * @date 2022/02/07
 **/
public class UserAuthServiceImpl implements UserDetailsService, AuthenticationProvider {

    @Lazy
    @Resource
    private IUserAuthService userAuthService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication != null && !(authentication instanceof ITokenAuthentication)) {
            return authentication;
        }
        ITokenAuthentication tokenAuthentication = (ITokenAuthentication)authentication;
        AssertUtil.isTrue(tokenAuthentication.getAuthException() != null, () -> tokenAuthentication.getAuthException());
        AssertUtil.isTrue(!tokenAuthentication.hasToken(), () -> new NotFoundTokenException("未获取到有效的Token"));
        userAuthService.authToken(tokenAuthentication.getTokenModel());
        UserDetails principal = userAuthService.loadUserInfoByToken(tokenAuthentication.getTokenModel());
        return TokenAuthentication.createFinal(tokenAuthentication.getTokenModel(), principal);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return BeanUtil.isBaseFrom(authentication, TokenAuthentication.class);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userAuthService.loadUserInfoByUniqueKey(username);
    }
}
