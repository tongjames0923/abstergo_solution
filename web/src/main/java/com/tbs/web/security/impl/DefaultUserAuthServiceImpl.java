package com.tbs.web.security.impl;

import com.tbs.common.util.AssertUtil;
import com.tbs.common.util.BeanUtil;
import com.tbs.common.util.StrUtil;
import com.tbs.web.exception.SimpleAuthException;
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
public class DefaultUserAuthServiceImpl implements UserDetailsService, AuthenticationProvider {

    @Lazy
    @Resource
    private IUserAuthService userAuthService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        TokenAuthentication tokenAuthentication = (TokenAuthentication)authentication;
        AssertUtil.isTrue(!tokenAuthentication.hasToken(), () -> new SimpleAuthException(
            StrUtil.isEmpty(tokenAuthentication.getMessage()) ? "未找到Token" : tokenAuthentication.getMessage()));
        userAuthService.authToken(tokenAuthentication.getCurrentTokenModel());
        UserDetails principal = userAuthService.loadUserInfoByToken(tokenAuthentication.getCurrentTokenModel());
        return TokenAuthentication.createFinal(tokenAuthentication.getCurrentTokenModel(), principal);
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
