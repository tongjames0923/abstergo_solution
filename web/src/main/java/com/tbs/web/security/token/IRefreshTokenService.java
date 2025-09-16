package com.tbs.web.security.token;

import com.tbs.web.enums.TokenCreateTypeEnum;
import org.springframework.security.core.userdetails.UserDetails;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 刷新令牌服务
 *
 * @author jiangnan
 * @date 2020/01/07
 */
public interface IRefreshTokenService {
    /**
     * 推送刷新令牌
     *
     * @param userDetails
     * @param response
     * @param createTypeEnum
     */
    void pushRefreshToken(UserDetails userDetails, HttpServletResponse response, TokenCreateTypeEnum createTypeEnum);

    /**
     * 移除刷新令牌
     *
     * @param request
     * @param response
     */
    void removeRefreshToken(HttpServletRequest request, HttpServletResponse response);

    /**
     * 获取刷新令牌
     *
     * @return
     */
    ITokenModel getRefreshToken(HttpServletRequest request);

    /**
     * 刷新AccessToken令牌
     *
     * @return
     */
    ITokenModel refreshAccessToken(HttpServletRequest request);

}
