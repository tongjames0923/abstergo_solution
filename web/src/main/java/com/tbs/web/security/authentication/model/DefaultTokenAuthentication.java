package com.tbs.web.security.authentication.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.tbs.web.security.token.ITokenModel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * 令牌认证（默认实现）
 * <p>
 * 实现 {@link ITokenAuthentication}：以 {@link ITokenModel} 为凭证载体。用户详情由认证中心在认证阶段
 * 一次性装配后注入，注入后本次认证对象即为状态快照：同一请求内多次读取返回同一对象，
 * 不会因请求过程中的数据变更而出现“前一个 getter 拿旧值、后一个 getter 拿新值”的不一致。
 * </p>
 * <p>
 * 跨请求的新鲜度由下一次认证取保：会话失效、权限变更均在下一个请求的认证阶段生效。
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

    /**
     * 认证阶段装配的用户详情快照，注入后不再变化
     */
    private UserDetails userDetails;

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
        return userDetails;
    }

    @JsonIgnore
    @Override
    public void setUserDetails(UserDetails userDetails) {
        this.userDetails = userDetails;
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
