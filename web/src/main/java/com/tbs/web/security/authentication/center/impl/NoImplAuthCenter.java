package com.tbs.web.security.authentication.center.impl;

import org.springframework.security.core.AuthenticationException;

import com.tbs.common.error.AssertUtil;
import com.tbs.web.exception.InvalidTokenException;
import com.tbs.web.security.authentication.center.IAuthenticationCenter;
import com.tbs.web.security.authentication.model.ITokenAuthentication;
import com.tbs.web.security.token.ITokenModel;

/**
 * 无实现认证中心
 * <p>
 * 占位实现：用于尚未接入具体认证中心的场景。构造即抛 {@link UnsupportedOperationException}，
 * 提醒使用者必须提供真实的 {@link IAuthenticationCenter} 实现。
 * </p>
 *
 * @author yin
 */
public class NoImplAuthCenter implements IAuthenticationCenter {

	public NoImplAuthCenter() {
		AssertUtil.throwError(() -> new UnsupportedOperationException("请实现一个验证中心"));
	}

	@Override
	public ITokenAuthentication createAuthenticationByTokenModel(ITokenModel model) throws InvalidTokenException {
		return null;
	}

	@Override
	public ITokenAuthentication authenticate(ITokenAuthentication authentication) throws AuthenticationException {
		return null;
	}
}
