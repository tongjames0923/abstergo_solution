package com.tbs.web.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 用户登陆注销接口
 *
 * @author abstergo
 */
public interface IUserAuthService {
    /**
     * 通过唯一标识获取用户信息
     *
     * @param uniqueKey
     * @return
     */
    UserDetails loadUserInfoByUniqueKey(String uniqueKey);

    /**
     * 通过token获取用户信息
     *
     * @param token
     * @return
     */
    UserDetails loadUserInfoByToken(ITokenModel token);

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
