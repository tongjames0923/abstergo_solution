package com.tbs.web.security.user;

import com.tbs.web.security.token.ITokenModel;

import java.util.List;

/**
 * 用户认证服务
 *
 * @author abstergo
 */
public interface IUserAuthService {
    /**
     * 登录
     *
     * @param username
     * @param password
     * @return
     */
    ITokenModel login(String username, String password);

    /**
     * 获取用户所有令牌
     *
     * @param username
     * @return
     */
    List<ITokenModel> getTokensByUserName(String username);

    /**
     * 撤销令牌
     *
     * @param token
     * @return
     */
    ITokenModel revokeToken(String token);

    /**
     * 禁用令牌
     *
     * @param token
     * @return
     */
    ITokenModel inActiveToken(String token);

    /**
     * 通知用户认证状态改变
     *
     * @param userName
     */
    void notifyUserAuthStatusChanged(String userName);
}
