package com.tbs.app.service;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.userdetails.UserDetails;

import com.tbs.common.data.IDataHolderManager;
import com.tbs.common.data.ValueWrapper;
import com.tbs.common.util.CollUtil;
import com.tbs.common.util.encrypt.EncryptUitl;
import com.tbs.web.security.impl.AbstractRefreshableTokenCreator;
import com.tbs.web.security.token.ITokenModel;

public class TestRefreshTokenCreator extends AbstractRefreshableTokenCreator {
	@Resource
	private IDataHolderManager dataHolderManager;

	@Resource
	HttpServletResponse response;

	@Resource
	HttpServletRequest request;

	@Override
	public void submitRefreshToken(ITokenModel tokenModel, UserDetails userDetails) {
		String token = tokenModel.getToken();
		String re = EncryptUitl.hash128Encrypt(token, userDetails.getUsername());
		dataHolderManager.getDataHolder("REFRESH_TOKEN").ifPresent((h) -> h.set(re, userDetails, true));
		Cookie cookie = new Cookie("REFRESH_TOKEN", re);
		cookie.setMaxAge(7 * 24 * 60 * 60);
		cookie.setHttpOnly(true);
		response.addCookie(cookie);
	}

	@Override
	public void removeRefreshToken(String refreshToken) {
		dataHolderManager.getDataHolder("REFRESH_TOKEN").ifPresent((h) -> h.remove(refreshToken));
	}

	@Override
	public String getRefreshToken() {
		Cookie[] cookies = request.getCookies();
		if (CollUtil.isEmpty(cookies)) {
			return null;
		}
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals("REFRESH_TOKEN")) {
				return cookie.getValue();
			}
		}
		return null;
	}

	@Override
	protected RefreshToken createTokenByRefreshToken(String refreshToken) {
		ValueWrapper<UserDetails> hasRefreshToken = new ValueWrapper<>();
		dataHolderManager.getDataHolder("REFRESH_TOKEN").ifPresent((h) -> h.get(refreshToken).ifPresent((v) -> {
			hasRefreshToken.set((UserDetails) v);
		}));
		return hasRefreshToken.get().map(userDetails -> {
			return new RefreshToken(refreshToken, createToken(userDetails), userDetails);
		}).orElse(null);
	}
}
