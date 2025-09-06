package com.tbs.web.security.authentication.model;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.tbs.web.security.token.ITokenModel;

import lombok.Getter;
import lombok.Setter;

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

	public TokenAuthentication() {
		super(Arrays.asList());
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
