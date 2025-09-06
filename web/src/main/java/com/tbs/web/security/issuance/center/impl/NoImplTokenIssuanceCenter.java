package com.tbs.web.security.issuance.center.impl;

import com.tbs.web.enums.TokenStatusEnum;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.userdetails.UserDetails;

import com.tbs.common.error.AssertUtil;
import com.tbs.web.security.issuance.center.ITokenIssuanceCenter;
import com.tbs.web.security.token.ITokenModel;

/**
 * 无实现Token签发中心
 * @author abstergo
 */
public class NoImplTokenIssuanceCenter implements ITokenIssuanceCenter {

	public NoImplTokenIssuanceCenter() {
		AssertUtil.throwError(() -> new UnsupportedOperationException("请实现一个Token签发中心"));
	}

	@Override
	public ITokenModel issueToken(@NotNull UserDetails userDetails, int tokenType, int tokenCreateType) {
		return null;
	}

	@Override
	public ITokenModel getTokenByToken(@NotNull String token) {
		return null;
	}

    @Override
    public void setTokenStatus(@NotNull String token, @NotNull TokenStatusEnum status) {

    }

}
