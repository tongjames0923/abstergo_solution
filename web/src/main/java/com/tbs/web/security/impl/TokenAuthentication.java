package com.tbs.web.security.impl;

import com.tbs.web.security.model.TokenModel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 令牌认证
 *
 * @author tongj
 */
public class TokenAuthentication extends AbstractAuthenticationToken {
    @Getter
    @Setter
    private UserDetails currentUserDetails;
    @Getter
    @Setter
    private TokenModel currentTokenModel;

    @Getter
    @Setter
    private String message;



    public TokenAuthentication(Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
    }

    public static TokenAuthentication create(TokenModel model) {
        TokenAuthentication authentication = new TokenAuthentication(new ArrayList<>(8));
        authentication.setCurrentTokenModel(model);
        return authentication;
    }

    public static TokenAuthentication createFinal(TokenModel model, UserDetails userDetails) {
        TokenAuthentication authentication = new TokenAuthentication(userDetails.getAuthorities());
        authentication.setCurrentTokenModel(model);
        authentication.setCurrentUserDetails(userDetails);
        authentication.setAuthenticated(true);
        return authentication;
    }

    public static TokenAuthentication badTokenCreate(String msg) {
        TokenAuthentication authentication = new TokenAuthentication(new ArrayList<>(8));
        authentication.setMessage(msg);
        return authentication;
    }

    /**
     * 是否有令牌
     *
     * @return
     */
    public boolean hasToken() {
        return getCurrentTokenModel() != null;
    }

    @Override
    public Object getCredentials() {
        return getCurrentTokenModel();
    }

    @Override
    public Object getPrincipal() {
        return getCurrentUserDetails();
    }
}
