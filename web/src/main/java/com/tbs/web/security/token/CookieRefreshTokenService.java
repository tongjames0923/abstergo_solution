package com.tbs.web.security.token;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tbs.common.util.CollUtil;

/**
 * 默认的Cookie刷新令牌服务实现
 *
 * @author abstergo
 */
public class CookieRefreshTokenService implements IRefreshTokenService {

	@Resource
	HttpServletRequest request;
	@Override
	public void pushRefreshToken(ITokenModel tokenModel, HttpServletResponse response) {
		Cookie cookie = new Cookie("REFRESH_TOKEN", tokenModel.getToken());
		cookie.setMaxAge(7 * 24 * 60 * 60);
		cookie.setHttpOnly(true);
		response.addCookie(cookie);
	}

	@Override
	public void removeRefreshToken(String refreshToken) {
		Cookie[] cookies = request.getCookies();
		if (CollUtil.isEmpty(cookies)) {
			return;
		}
		for (Cookie cookie : cookies) {
			if ("REFRESH_TOKEN".equals(cookie.getName())) {
				cookie.setMaxAge(0);
				cookie.setHttpOnly(true);
				return;
			}
		}
	}

	@Override
	public String getRefreshToken() {
		Cookie[] cookies = request.getCookies();
		if (CollUtil.isEmpty(cookies)) {
			return null;
		}
		for (Cookie cookie : cookies) {
			if ("REFRESH_TOKEN".equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
	}
}
