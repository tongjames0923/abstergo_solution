package com.tbs.app.service.business.token.impl;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tbs.app.service.business.token.IRefreshTokenService;
import com.tbs.common.util.CollUtil;
import com.tbs.web.security.token.ITokenModel;

/**
 *
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
