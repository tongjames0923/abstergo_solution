package com.tbs.web.security.token;

import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.userdetails.UserDetails;

import com.tbs.web.enums.TokenCreateTypeEnum;

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
	 */
	void removeRefreshToken();

	/**
	 * 获取刷新令牌
	 *
	 * @return
	 */
	ITokenModel getRefreshToken();

	/**
	 * 刷新AccessToken令牌
	 *
	 * @return
	 */
	ITokenModel refreshAccessToken();

}
