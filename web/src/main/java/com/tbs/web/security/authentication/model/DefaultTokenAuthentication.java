package com.tbs.web.security.authentication.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.user.IUserLoginInfoService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * 令牌认证
 *
 * @author tongj
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class DefaultTokenAuthentication implements ITokenAuthentication {

    @Getter
    @Setter
    private ITokenModel tokenModel;

    private AuthenticationException authenticationException;

    private boolean authenticated = false;

    public DefaultTokenAuthentication(ITokenModel tokenModel, AuthenticationException authenticationException) {
        this.tokenModel = tokenModel;
        this.authenticationException = authenticationException;
    }

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getUserDetails().getAuthorities();
    }

    @JsonIgnore
    @Override
    public Object getCredentials() {
        return getTokenModel();
    }

    @JsonIgnore
    @Override
    public Object getDetails() {
        return tokenModel;
    }

    @JsonIgnore
    @Override
    public Object getPrincipal() {
        return getUserDetails();
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.authenticated = isAuthenticated;
    }

    @JsonIgnore
    @Override
    public UserDetails getUserDetails() {
        return SpringUtil.getBean(IUserLoginInfoService.class).getUserDetails(getTokenModel().getToken());
    }

    @JsonIgnore
    @Override
    public AuthenticationException getAuthException() {
        return authenticationException;
    }

    @Override
    public void setAuthException(AuthenticationException e) {
        this.authenticationException = e;
    }

    @JsonIgnore
    @Override
    public String getName() {
        return getUserDetails().getUsername();
    }
}
