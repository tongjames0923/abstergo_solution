package com.tbs.web.security.user;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

import com.tbs.web.security.token.ITokenModel;

/**
 * 用户登陆注销接口
 *
 * @author abstergo
 */
public interface IUserAuthService {

	/**
	 * 用户登陆
	 *
	 * @param username
	 * @param password
	 * @return
	 */
	ITokenModel login(@NotNull String username, @NotNull String password);

	/**
	 * 认证token
	 *
	 * @param simpleTokenModel
	 * @throws AuthenticationException
	 */
	void authToken(@NotNull ITokenModel simpleTokenModel) throws AuthenticationException;

	/**
	 * 提交token
	 *
	 * @param simpleTokenModel
	 */
	void submitToken(@NotNull ITokenModel simpleTokenModel, @NotNull UserDetails details);

	/**
	 * 用户登出
	 *
	 * @param simpleTokenModel
	 * @return
	 */
	boolean logout(@NotNull ITokenModel simpleTokenModel);

}
