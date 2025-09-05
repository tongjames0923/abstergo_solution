package com.tbs.app.controller.publics;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Lazy;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.tbs.app.form.LoginForm;
import com.tbs.common.error.AssertUtil;
import com.tbs.web.BaseController;
import com.tbs.web.exception.InvalidTokenException;
import com.tbs.web.security.impl.AbstractRefreshableTokenCreator;
import com.tbs.web.security.token.ITokenCreator;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.user.IUserAuthService;

import io.swagger.annotations.Api;

/**
 * 公开的用户控制器
 *
 * @author abstergo
 */
@RestController
@RequestMapping("/public/user")
@Api(value = "公开用户控制器")
public class PublicUserController extends BaseController {

	@Lazy
	@Resource
	IUserAuthService userAuthService;

	@PostMapping("/login")
	public ITokenModel login(@RequestBody @Validated LoginForm loginForm) {
		ITokenModel tokenModel = userAuthService.login(loginForm.getUsername(), loginForm.getPassword());
		return tokenModel;
	}

	@Resource
	private HttpServletRequest request;

	@Resource
	AbstractRefreshableTokenCreator tokenCreator;

	@GetMapping("refresh")
	public ITokenModel refresh() {

		ITokenModel tokenModel = tokenCreator.refreshToken();
		AssertUtil.isTrue(tokenModel == null, () -> new InvalidTokenException("Token失效且刷新Token失败"));
		return tokenModel;
	}

	@Override
	public String getBusinessName() {
		return "公开用户控制器";
	}

}
