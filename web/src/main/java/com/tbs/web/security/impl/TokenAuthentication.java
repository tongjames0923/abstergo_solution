package com.tbs.web.security.impl;

import com.tbs.common.util.StrUtil;
import com.tbs.web.security.ITokenAuthentication;
import com.tbs.web.security.ITokenModel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 令牌认证
 *
 * @author tongj
 */
public class TokenAuthentication extends AbstractAuthenticationToken implements ITokenAuthentication {
    @Getter
    @Setter
    private UserDetails userDetails;
    @Getter
    @Setter
    private ITokenModel tokenModel;

    private AuthenticationException authenticationException;

    public TokenAuthentication(Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
    }

    /**
     * 创建令牌
     *
     * @param model
     * @return
     */
    public static TokenAuthentication create(ITokenModel model) {
        TokenAuthentication authentication = new TokenAuthentication(new ArrayList<>(8));
        authentication.setTokenModel(model);
        return authentication;
    }

    /**
     * 创建最终令牌
     *
     * @param model
     * @param userDetails
     * @return
     */
    public static TokenAuthentication createFinal(ITokenModel model, UserDetails userDetails) {
        TokenAuthentication authentication = new TokenAuthentication(userDetails.getAuthorities());
        authentication.setTokenModel(model);
        authentication.setUserDetails(userDetails);
        authentication.setAuthenticated(true);
        return authentication;
    }

    @Override
    public Object getCredentials() {
        return getTokenModel();
    }

    @Override
    public Object getPrincipal() {
        return getUserDetails();
    }

    @Override
    public AuthenticationException getAuthException() {
        return authenticationException;
    }

    @Override
    public void setAuthException(AuthenticationException e) {
        this.authenticationException = e;
    }
}
