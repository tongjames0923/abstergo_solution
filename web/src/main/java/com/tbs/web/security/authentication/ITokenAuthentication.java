package com.tbs.web.security.authentication;

import com.tbs.web.security.token.ITokenModel;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * token认证接入接口
 *
 * @author tongj
 */
public interface ITokenAuthentication extends Authentication {
    /**
     * 获取token
     *
     * @return
     */
    ITokenModel getTokenModel();

    /**
     * 设置token
     *
     * @param model
     */
    void setTokenModel(ITokenModel model);

    /**
     * 是否有token
     *
     * @return
     */
    default boolean hasToken() {
        return getTokenModel() != null;
    }

    /**
     * 获取用户信息
     *
     * @return
     */
    UserDetails getUserDetails();

    /**
     * 获取认证异常
     *
     * @return
     */
    AuthenticationException getAuthException();

    /**
     * 设置认证异常
     *
     * @param e
     */
    void setAuthException(AuthenticationException e);
}
