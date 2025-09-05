package com.tbs.web.security.token;

import org.springframework.security.core.userdetails.UserDetails;

public interface ITokenCreator {
	/**
	 * 创建token
	 *
	 * @param userDetails 用户信息
	 * @return token
	 */
	ITokenModel createToken(UserDetails userDetails);
}
