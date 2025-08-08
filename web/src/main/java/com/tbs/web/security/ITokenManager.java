package com.tbs.web.security;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 登录Token管理器
 *
 * @author tongj
 */
public interface ITokenManager {
    /**
     * 提交用户登录Token
     *
     * @param token       登录Token
     * @param userDetails 用户信息
     */
    void submitToken(@NotNull ITokenModel token, @NotNull UserDetails userDetails);

    /**
     * 移除用户登录Token
     *
     * @param token 登录Token
     */
    void removeToken(@NotNull ITokenModel token);

    /**
     * 通过token获取用户信息
     *
     * @param token
     * @return
     */
    @NotNull UserDetails loadUserInfoByToken(@NotNull ITokenModel token);
}
