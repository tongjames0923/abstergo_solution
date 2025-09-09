package com.tbs.web.security.authentication.center.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.StrUtil;
import com.tbs.web.exception.SimpleAuthException;
import com.tbs.web.security.authentication.center.AbstractAuthenticationCenter;
import com.tbs.web.security.authentication.model.ITokenAuthentication;
import com.tbs.web.security.authentication.model.TokenAuthentication;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.user.IUserInfoService;
import com.tbs.web.uitl.AuthUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Resource;

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
        AssertUtil.isTrue(authentication == null, () -> new SimpleAuthException("认证信息为空"));
        AssertUtil.isTrue(authentication.getTokenModel() == null, () -> new SimpleAuthException("令牌信息为空"));
        AssertUtil.isTrue(authentication.getUserDetails() == null, () -> new SimpleAuthException("用户信息为空"));
        try {
            AuthUtil.checkUserDetails(authentication.getUserDetails());
        } catch (Exception e) {
            AssertUtil.throwError(
                () -> new SimpleAuthException(StrUtil.format("用户信息验证失败。详情:{}", e.getLocalizedMessage())));
        }
		authentication.setAuthenticated(true);
		return authentication;
	}
}
