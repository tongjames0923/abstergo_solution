package com.tbs.web.security;

import org.springframework.security.core.Authentication;
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
}
