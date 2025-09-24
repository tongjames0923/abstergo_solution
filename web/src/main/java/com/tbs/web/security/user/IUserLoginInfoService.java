package com.tbs.web.security.user;

import com.tbs.web.security.token.ITokenModel;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

/**
 * 用户登录信息处理服务
 *
 */
public interface IUserLoginInfoService {
    /**
     * 用户登录成功,处理登录成功后的用户数据
     *
     * @param userDetails
     * @param tokenModel
     */
    void insertUserLoginInfo(UserDetails userDetails, ITokenModel tokenModel);

    /**
     * 用户登出
     *
     * @param token
     */
    void removeUserLoginInfo(String token);

    /**
     * 替换用户数据
     *
     * @param userDetails
     * @param token
     */
    void replaceUserDetails(UserDetails userDetails, ITokenModel token);

    /**
     * 根据Token获取获取用户数据
     *
     * @param token
     * @return
     */
    UserDetails getUserDetails(String token);

    /**
     * 获取用户可用的Token
     *
     * @param username
     * @return
     */
    List<String> getUserAvaliableSessionId(String username);

    /**
     * 获取用户的登录会话列表
     *
     * @param username
     * @return
     */
    List<LoginSessionVO> listUserSessions(String username);
}
