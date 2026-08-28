package com.tbs.web.security.authentication.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.tbs.common.util.base.CollUtil;
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
 * 令牌认证（默认实现）
 * <p>
 * 实现 {@link ITokenAuthentication}：以 {@link ITokenModel} 为凭证载体，用户详情按需从
 * {@link IUserLoginInfoService} 回源（每次访问实时查询，保证令牌失效后认证上下文同步失效）。
 * 认证状态、异常等信息随对象持有，便于异常处理器还原失败原因。
 * </p>
 * <p>
 * 序列化策略：通过 {@code @JsonTypeInfo} 保留具体类名，避免反序列化丢失类型；敏感字段（ authorities、
 * credentials 等）以 {@code @JsonIgnore} 排除，防止令牌信息泄露。
 * </p>
 *
 * @author tongj
 * @see ITokenAuthentication 令牌认证契约
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
        // 实时回源：每次访问都从登录信息服务查询，使令牌失效后认证上下文立即失效（避免缓存陈旧）
        return CollUtil.getFirst(
            SpringUtil.getBean(IUserLoginInfoService.class).getUserDetails(getTokenModel().getToken()));
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
