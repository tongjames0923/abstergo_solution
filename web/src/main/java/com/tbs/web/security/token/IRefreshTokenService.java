package com.tbs.web.security.token;

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
	 * @param tokenModel
	 * @param response
	 */
	void pushRefreshToken(ITokenModel tokenModel, HttpServletResponse response);

	/**
	 * 移除刷新令牌
	 *
	 * @param refreshToken
	 */
	void removeRefreshToken(String refreshToken);

	/**
	 * 获取刷新令牌
	 *
	 * @return
	 */
	String getRefreshToken();
}
