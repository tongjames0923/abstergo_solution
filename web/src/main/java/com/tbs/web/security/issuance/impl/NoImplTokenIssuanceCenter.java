package com.tbs.web.security.issuance.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.web.enums.TokenStatusEnum;
import com.tbs.web.security.issuance.ITokenIssuanceCenter;
import com.tbs.web.security.token.ITokenModel;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

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

    @Override
    public Collection<ITokenModel> getTokensByUserNameAndTokenType(String userName, int tokenType) {
        return List.of();
    }

}
