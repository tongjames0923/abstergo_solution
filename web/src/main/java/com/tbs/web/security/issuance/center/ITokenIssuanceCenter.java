package com.tbs.web.security.issuance.center;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.userdetails.UserDetails;

import com.tbs.web.enums.TokenStatusEnum;
import com.tbs.web.security.token.ITokenModel;

/**
 * 令牌发放中心
 *
 * @author abstergo
 */
public interface ITokenIssuanceCenter {
	/**
	 * 发放令牌
	 *
	 * @param userDetails
	 * @param tokenType
	 * @param tokenCreateType
	 * @return
	 */
	ITokenModel issueToken(@NotNull UserDetails userDetails, int tokenType, int tokenCreateType);

	/**
	 * 获取令牌
	 *
	 * @param token
	 * @return
	 */
	ITokenModel getTokenByToken(@NotNull String token);

    /**
	 * 设置令牌状态
	 *
	 * @param token
	 * @param status
	 */
	void setTokenStatus(@NotNull String token, @NotNull TokenStatusEnum status);

	/**
	 * 获取用户令牌
	 *
	 * @param userName
	 * @param tokenType
	 * @return
	 */
	Collection<ITokenModel> getTokensByUserNameAndTokenType(String userName, int tokenType);

}
