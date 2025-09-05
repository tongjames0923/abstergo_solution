package com.tbs.web.security.impl;

import javax.annotation.Resource;

import org.springframework.security.core.userdetails.UserDetails;

import com.tbs.web.security.token.AbstractTokenCreator;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.token.model.SimpleTokenModel;
import com.tbs.web.security.user.IUserAuthService;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public abstract class AbstractRefreshableTokenCreator extends AbstractTokenCreator {

	@Resource
	private IUserAuthService authService;

	/**
	 * 生成并提交刷新令牌
	 *
	 * @param tokenModel  刷新令牌
	 * @param userDetails 用户信息
	 */
	public abstract void submitRefreshToken(ITokenModel tokenModel, UserDetails userDetails);

	/**
	 * 移除刷新令牌
	 *
	 * @param refreshToken 刷新令牌
	 */
	public abstract void removeRefreshToken(String refreshToken);

	/**
	 * 获取刷新令牌
	 *
	 * @return 刷新令牌
	 */
	public abstract String getRefreshToken();

	/**
	 * 通过刷新令牌创建令牌
	 *
	 * @param refreshToken 刷新令牌
	 * @return 令牌
	 */
	protected abstract RefreshToken createTokenByRefreshToken(String refreshToken);

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class RefreshToken {
		private String refreshToken;
		private ITokenModel token;
		private UserDetails details;
	}

	/**
	 * 刷新令牌
	 *
	 * @param refreshToken 刷新令牌
	 * @return 刷新令牌
	 */
	public ITokenModel refreshToken() {
		RefreshToken refreshToken = createTokenByRefreshToken(getRefreshToken());
		if (refreshToken != null) {
			authService.submitToken(refreshToken.token, refreshToken.details);
			return refreshToken.token;
		}
		return null;
	}

	@Override
	protected ITokenModel interceptToken(SimpleTokenModel token) {
		return token;
	}
}
