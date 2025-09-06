package com.tbs.web.security.authentication.center.impl;

import javax.annotation.Resource;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

import com.tbs.web.security.authentication.center.AbstractAuthenticationCenter;
import com.tbs.web.security.authentication.model.ITokenAuthentication;
import com.tbs.web.security.authentication.model.TokenAuthentication;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.user.IUserInfoService;

/**
 * 默认的认证中心
 *
 * @author jiangnan
 *
 */
public class SimpleAuthenticationCenter extends AbstractAuthenticationCenter {

	@Resource
	IUserInfoService userInfoService;

	@Override
	protected ITokenAuthentication createAuth(@NotNull ITokenModel model) {
		UserDetails details = userInfoService.loadUserInfoByUniqueKey(model.getUserName());
		TokenAuthentication tokenAuthentication = new TokenAuthentication(details.getAuthorities());
		tokenAuthentication.setTokenModel(model);
		tokenAuthentication.setUserDetails(details);
		return tokenAuthentication;
	}

	@Override
	public ITokenAuthentication authenticate(ITokenAuthentication authentication) throws AuthenticationException {
		if (authentication == null) {
			return null;
		}

		authentication.setAuthenticated(true);
		return authentication;
	}
}
