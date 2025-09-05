package com.tbs.app.controller.publics;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.tbs.app.form.LoginForm;
import com.tbs.web.BaseController;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.token.model.SimpleTokenModel;
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

	@Resource
	IUserAuthService userAuthService;

	@Resource
	HttpServletResponse response;

	@PostMapping("/login")
	public ITokenModel login(@RequestBody @Validated LoginForm loginForm) {

		Cookie refreshTokenCookie = new Cookie("refreshToken", "yes");
		refreshTokenCookie.setMaxAge(60 * 60 * 24 * 7);
		refreshTokenCookie.setHttpOnly(true);
		response.addCookie(refreshTokenCookie);
		return userAuthService.login(loginForm.getUsername(), loginForm.getPassword());
	}

	@Resource
	private HttpServletRequest request;

	@GetMapping("refresh")
	public ITokenModel refresh() {
		var map = request.getCookies();
		return SimpleTokenModel.create("yes", null, null, null, null);
	}

	@Override
	public String getBusinessName() {
		return "公开用户控制器";
	}

}
