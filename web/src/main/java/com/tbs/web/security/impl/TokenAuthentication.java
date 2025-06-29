package com.tbs.web.security.impl;

import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.web.security.model.TokenModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * 令牌认证
 *
 * @author tongj
 */
public class TokenAuthentication extends AbstractAuthenticationToken {
    private UserDetails principal;
    private TokenModel model;

    public TokenAuthentication(Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
    }

    public static TokenAuthentication createSuccess(UserDetails principal, TokenModel model) {
        TokenAuthentication authentication = new TokenAuthentication(principal.getAuthorities());
        authentication.principal = principal;
        authentication.model = model;
        authentication.setAuthenticated(true);
        return authentication;
    }

    public static TokenAuthentication createFail(UserDetails principal, TokenModel model) {
        TokenAuthentication authentication = new TokenAuthentication(null);
        authentication.principal = principal;
        authentication.model = model;
        authentication.setAuthenticated(false);
        return authentication;
    }

    /**
     * 获取当前用户的认证信息
     */
    public TokenModel getCurrentTokenModel() {
        return model;
    }

    @Override
    public Object getCredentials() {
        return getCurrentTokenModel();
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
