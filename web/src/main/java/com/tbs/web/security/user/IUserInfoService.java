package com.tbs.web.security.user;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 用户信息服务, 用于获取用户信息
 *
 * @author tongj
 */
public interface IUserInfoService {
    public static String USER_DETAILS_CACHE = "user_details";


    /**
     * 通过唯一标识获取标准用户信息，不被登录时状态影响
     *
     * @param uniqueKey
     * @return
     */
    UserDetails loadUserInfoByUniqueKey(@NotNull String uniqueKey);
}
