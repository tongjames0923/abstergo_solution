package com.tbs.web.gate.center.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.tbs.web.gate.ITokenPicker;
import com.tbs.web.gate.center.AbstractAntPathSupportGateWayCenter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.tbs.common.error.AssertUtil;
import com.tbs.web.config.property.WebProperty;
import com.tbs.web.exception.NotFoundTokenException;
import com.tbs.web.security.authentication.center.IAuthenticationCenter;
import com.tbs.web.security.authentication.model.ITokenAuthentication;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.uitl.AuthUtil;

/**
 * 抽象登录安全管理器
 *
 *
 * @author abstergo
 */
public class LoginGateWayCenter extends AbstractAntPathSupportGateWayCenter {

	private ITokenPicker loginTokenPicker;

	@Resource
	IAuthenticationCenter authenticationCenter;

	@Resource
	private AuthenticationManager manager;

	@Resource
	WebProperty webProperty;

	public LoginGateWayCenter(ITokenPicker loginTokenPicker) {
		super(null);
		this.loginTokenPicker = loginTokenPicker;
	}

	@PostConstruct
	void addMatcher() {
		List<RequestMatcher> matchers = new ArrayList<>(webProperty.getSecuredPaths().size());
		matchers.addAll(webProperty.getSecuredPaths().stream().map(path -> new AntPathRequestMatcher(path))
				.collect(java.util.stream.Collectors.toList()));
		setMatcher(new OrRequestMatcher(matchers));
	}

	@Override
	public ITokenPicker getTokenPicker() {
		return loginTokenPicker;
	}

	@Override
	public void onNoneToken() {
		AssertUtil.throwError(() -> new NotFoundTokenException("身份令牌不存在。"));
	}

	@Override
	public void handleToken(ITokenModel tokenModel) {
		ITokenAuthentication tokenAuthentication = authenticationCenter.createAuthenticationByTokenModel(tokenModel);
		AuthUtil.getInstance().setAuthentication((ITokenAuthentication) manager.authenticate(tokenAuthentication));
	}

	@Override
	public boolean onErrorWillBroken(Throwable e) {
		return true;
	}
}
