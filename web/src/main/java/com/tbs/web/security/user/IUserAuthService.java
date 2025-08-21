package com.tbs.web.security.user;

import com.tbs.web.security.token.ITokenModel;
import org.springframework.security.core.AuthenticationException;

/**
 * 用户登陆注销接口
 *
 * @author abstergo
 */
public interface IUserAuthService {

    /**
     * 用户登陆
     *
     * @param username
     * @param password
     * @return
     */
    ITokenModel login(String username, String password);

    /**
     * 认证token
     *
     * @param simpleTokenModel
     * @throws AuthenticationException
     */
    void authToken(ITokenModel simpleTokenModel) throws AuthenticationException;

    /**
     * 用户登出
     *
     * @param simpleTokenModel
     * @return
     */
    boolean logout(ITokenModel simpleTokenModel);
}
