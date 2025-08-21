package com.tbs.web.security;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.tbs.common.error.AssertUtil;
import com.tbs.web.config.property.WebProperty;
import com.tbs.web.exception.NotFoundTokenException;
import com.tbs.web.security.impl.TokenAuthentication;
import com.tbs.web.uitl.AuthUtil;

/**
 * 抽象登录安全管理器
 *
 **/
public class LoginSecurityManager extends AbstractAntPathSupportSecurityManager {

	private ITokenPicker loginTokenPicker;

	@Resource
	private AuthenticationManager manager;

	@Resource
	WebProperty webProperty;

	public LoginSecurityManager(ITokenPicker loginTokenPicker) {
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
		ITokenAuthentication tokenAuthentication = TokenAuthentication.create(tokenModel);
		AuthUtil.getInstance().setAuthentication((ITokenAuthentication) manager.authenticate(tokenAuthentication));
	}

	@Override
	public boolean onErrorWillBroken(Throwable e) {
		return true;
	}
}
