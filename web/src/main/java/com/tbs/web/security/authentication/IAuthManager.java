package com.tbs.web.security.authentication;

import com.tbs.web.security.authentication.model.ITokenAuthentication;
import com.tbs.web.security.token.ITokenModel;
import org.springframework.security.core.userdetails.UserDetails;

public interface IAuthManager {

    /**
     * 判断当前用户是否已登录
     *
     * @return 如果用户已登录返回true，否则返回false
     */
    boolean isUserLoggedIn();

    /**
     * 获取当前用户的认证信息
     *
     * @return 当前用户的认证信息
     */
    UserDetails getCurrentUser();

    /**
     * 获取当前用户的令牌模型
     *
     * @return 当前用户的访问令牌模型
     */
    ITokenModel getCurrentTokenModel();

    /**
     * 设置当前用户的认证信息
     *
     * @param authentication 用户的认证信息
     */
    void setAuthentication(ITokenAuthentication authentication);

    /**
     * 清除当前用户的认证信息
     */
    void clearAuthentication();

    /**
     * 清除指定令牌对应的认证信息
     *
     * @param token 令牌
     */
    void clearAuthentication(String token);

    /**
     * 通知用户认证状态变更
     *
     * @param userName 用户名
     */
    void notifyUserAuthStatusChanged(String userName);

    /**
     * 获取当前用户的认证信息
     *
     * @return 当前用户的认证信息
     */
    ITokenAuthentication getCurrentAuthentication();

    /**
     * 获取指定令牌对应的认证信息
     *
     * @param token 令牌
     * @return 对应的认证信息
     */
    ITokenAuthentication getAuthentication(String token);
}
