package com.tbs.web.security.user;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.userdetails.UserDetails;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.web.enums.TokenCreateTypeEnum;
import com.tbs.web.enums.TokenStatusEnum;
import com.tbs.web.enums.TokenTypeEnum;
import com.tbs.web.security.issuance.center.ITokenIssuanceCenter;
import com.tbs.web.security.token.IRefreshTokenService;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.uitl.AuthUtil;

/**
 * @author abstergo
 */
public abstract class AbstractUserAuthService implements IUserAuthService {

	@Resource
	ITokenIssuanceCenter tokenIssuanceCenter;

	@Resource
	IUserInfoService userInfoService;

	@Resource
	IRefreshTokenService refreshTokenService;

	/**
	 * 验证用户名密码
	 *
	 * @param username
	 * @param password
	 * @return
	 */
	protected abstract boolean validate(String username, String password);

	@Resource
	HttpServletResponse response;

	@Override
	public ITokenModel login(String username, String password) {
		boolean validate = validate(username, password);
		AssertUtil.isTrue(!validate, () -> new FormattedRuntimeException("用户名或密码错误"));
		UserDetails details = userInfoService.loadUserInfoByUniqueKey(username);
		AuthUtil.checkUserDetails(details);
		var token = tokenIssuanceCenter.issueToken(details, TokenTypeEnum.ACCESS_TOKEN.getCode(),
				TokenCreateTypeEnum.LOGIN.getCode());

		var longTermToken = tokenIssuanceCenter.issueToken(details, TokenTypeEnum.REFRESH_TOKEN.getCode(),
				TokenCreateTypeEnum.LOGIN.getCode());
		refreshTokenService.pushRefreshToken(longTermToken, response);
		return token;
	}

	@Override
	public ITokenModel revokeToken(String token) {
		ITokenModel tokenModel = tokenIssuanceCenter.getTokenByToken(token);
		if (tokenModel == null) {
			return null;
		}
		if (TokenStatusEnum.ACTIVE.equals(tokenModel.getStatus())) {
			tokenIssuanceCenter.setTokenStatus(token, TokenStatusEnum.REVOKE);
			tokenModel.setStatus(TokenStatusEnum.REVOKE);
		}
		return tokenModel;
	}

	@Override
	public ITokenModel inActiveToken(String token) {
		ITokenModel tokenModel = tokenIssuanceCenter.getTokenByToken(token);
		if (tokenModel == null) {
			return null;
		}
		if (TokenStatusEnum.ACTIVE.equals(tokenModel.getStatus())) {
			tokenIssuanceCenter.setTokenStatus(token, TokenStatusEnum.INACTIVE);
			tokenModel.setStatus(TokenStatusEnum.INACTIVE);
		}
		return tokenModel;
	}
}
