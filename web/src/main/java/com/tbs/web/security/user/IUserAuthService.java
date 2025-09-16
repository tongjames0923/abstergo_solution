package com.tbs.web.security.user;

import com.tbs.web.enums.TokenTypeEnum;
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
     * 登出
     *
     * @param token
     */
    void logout(ITokenModel model);

    /**
     * 获取用户所有令牌
     *
     * @param username
     * @param type
     * @return
     */
    List<ITokenModel> getTokensByUserNameAndTokenType(String username, TokenTypeEnum type);

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
