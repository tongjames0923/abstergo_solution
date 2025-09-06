package com.tbs.app.controller.publics;

import java.util.List;
import java.util.Objects;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.tbs.app.form.LoginForm;
import com.tbs.app.service.business.token.IRefreshTokenService;
import com.tbs.common.util.StrUtil;
import com.tbs.web.BaseController;
import com.tbs.web.enums.TokenCreateTypeEnum;
import com.tbs.web.enums.TokenStatusEnum;
import com.tbs.web.enums.TokenTypeEnum;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.security.issuance.center.ITokenIssuanceCenter;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.user.IUserAuthService;
import com.tbs.web.security.user.IUserInfoService;
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

	@Resource
	IUserAuthService authService;

	@Resource
	IRefreshTokenService refreshTokenService;

	@Resource
	private ITokenIssuanceCenter tokenIssuanceCenter;

	@Resource
	private IUserInfoService userInfoService;

	@Resource
	HttpServletResponse response;

	@PostMapping("/login")
	public ITokenModel login(@RequestBody @Validated LoginForm loginForm) {
		return authService.login(loginForm.getUsername(), loginForm.getPassword());
	}

	@GetMapping("refresh")
	public AjaxResult refresh() {
		String refreshToken = refreshTokenService.getRefreshToken();
		if (StrUtil.isEmpty(refreshToken)) {
			return AjaxUtil.getInstance().custom(AjaxResult.REFRESH_FAIL_CODE, "不存在受支持的刷新令牌", null);
		}
		ITokenModel tokenModel = tokenIssuanceCenter.getTokenByToken(refreshToken);
		if (tokenModel == null) {
			return AjaxUtil.getInstance().custom(AjaxResult.REFRESH_FAIL_CODE, "未查询到有效的刷新令牌数据", null);
		}
		if (!Objects.equals(TokenTypeEnum.REFRESH_TOKEN, tokenModel.getTokenType())) {
			return AjaxUtil.getInstance().custom(AjaxResult.REFRESH_FAIL_CODE, "令牌类型异常", null);
		}
		if (!Objects.equals(tokenModel.getStatus(), TokenStatusEnum.ACTIVE)) {
			return AjaxUtil.getInstance().custom(AjaxResult.REFRESH_FAIL_CODE, "刷新令牌已失效", null);
		}
		UserDetails details = userInfoService.loadUserInfoByUniqueKey(tokenModel.getUserName());
		if (details == null) {
			return AjaxUtil.getInstance().custom(AjaxResult.REFRESH_FAIL_CODE, "用户不存在", null);
		}
		if (!details.isEnabled()) {
			return AjaxUtil.getInstance().custom(AjaxResult.REFRESH_FAIL_CODE, "用户已禁用", null);
		}
		if (!details.isAccountNonExpired()) {
			return AjaxUtil.getInstance().custom(AjaxResult.REFRESH_FAIL_CODE, "用户已过期", null);
		}
		if (!details.isAccountNonLocked()) {
			return AjaxUtil.getInstance().custom(AjaxResult.REFRESH_FAIL_CODE, "用户已锁定", null);
		}
		var newModel = tokenIssuanceCenter.issueToken(details, TokenTypeEnum.ACCESS_TOKEN.getCode(),
				TokenCreateTypeEnum.REFRESH.getCode());
		refreshTokenService.pushRefreshToken(newModel, response);
		return AjaxUtil.getInstance().success(newModel);
	}

	@Override
	public String getBusinessName() {
		return "公开用户控制器";
	}

}
