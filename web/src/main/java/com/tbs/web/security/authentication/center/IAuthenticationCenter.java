package com.tbs.web.security.authentication.center;

import org.springframework.security.core.AuthenticationException;

import com.tbs.web.exception.InvalidTokenException;
import com.tbs.web.security.authentication.model.ITokenAuthentication;
import com.tbs.web.security.token.ITokenModel;

/**
 * 认证中心
 *
 * @author abstergo
 */
public interface IAuthenticationCenter {

	/**
	 * 创建认证
	 *
	 * @param model
	 * @return
	 * @throws AuthenticationException
	 * @throws InvalidTokenException
	 */
	ITokenAuthentication createAuthenticationByTokenModel(ITokenModel model) throws InvalidTokenException;

	/**
	 * 认证
	 *
	 * @param authentication
	 * @return
	 * @throws AuthenticationException
	 */
	ITokenAuthentication authenticate(ITokenAuthentication authentication) throws AuthenticationException;

}
