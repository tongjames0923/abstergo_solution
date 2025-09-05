package com.tbs.app.controller.publics;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Lazy;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.tbs.app.form.LoginForm;
import com.tbs.common.util.SpringUtil;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.security.impl.AbstractRefreshableTokenCreator;
import com.tbs.web.security.token.ITokenCreator;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.user.IUserAuthService;
import com.tbs.web.uitl.AjaxUtil;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;

/**
 * 公开的用户控制器
 *
 * @author abstergo
 */
@RestController
@RequestMapping("/public/user")
@Api(value = "公开用户控制器")
@Slf4j
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
	ITokenCreator tokenCreator;

	@GetMapping("refresh")
	public AjaxResult refresh() {
		try {
			ITokenModel tokenModel = SpringUtil.getBean(AbstractRefreshableTokenCreator.class).refreshToken();
			if (tokenModel == null) {
				return AjaxUtil.getInstance().custom(468, "Token失效且刷新Token失败", null);
			}
			return AjaxUtil.getInstance().success(tokenModel);
		} catch (Exception e) {
			log.error("刷新Token失败", e);
			return AjaxUtil.getInstance().custom(468, e.getMessage(), null);
		}

	}

	@Override
	public String getBusinessName() {
		return "公开用户控制器";
	}

}
