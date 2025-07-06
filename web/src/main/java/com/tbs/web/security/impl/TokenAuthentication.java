package com.tbs.web.security.impl;

import com.tbs.common.util.StrUtil;
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

    /**
     * 创建令牌
     *
     * @param model
     * @return
     */
    public static TokenAuthentication create(TokenModel model) {
        TokenAuthentication authentication = new TokenAuthentication(new ArrayList<>(8));
        authentication.setCurrentTokenModel(model);
        return authentication;
    }

    /**
     * 创建最终令牌
     *
     * @param model
     * @param userDetails
     * @return
     */
    public static TokenAuthentication createFinal(TokenModel model, UserDetails userDetails) {
        TokenAuthentication authentication = new TokenAuthentication(userDetails.getAuthorities());
        authentication.setCurrentTokenModel(model);
        authentication.setCurrentUserDetails(userDetails);
        authentication.setAuthenticated(true);
        return authentication;
    }

    /**
     * 创建错误令牌
     *
     * @param msg
     * @param p
     * @return
     */
    public static <E extends Throwable> TokenAuthentication badTokenCreate(E e, String msg, Object... p) {
        TokenAuthentication authentication = new TokenAuthentication(null);
        StringBuilder message = new StringBuilder();
        if (e != null) {
            message.append(StrUtil.format("异常类型:{},异常信息:{}", e.getClass().getName(), e.getMessage()));
        }
        if (StrUtil.isNotEmpty(msg)) {
            message.append(StrUtil.format(" 更多信息:{}", StrUtil.format(msg, p)));
        }
        authentication.setMessage(message.toString());
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
