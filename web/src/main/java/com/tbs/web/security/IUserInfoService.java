package com.tbs.web.security;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 用户信息服务
 *
 * @author tongj
 */
public interface IUserInfoService {

    /**
     * 通过唯一标识获取用户信息
     *
     * @param uniqueKey
     * @return
     */
    UserDetails loadUserInfoByUniqueKey(@NotNull String uniqueKey);
}
