package com.tbs.web.security.issuance.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.i18n.I18nUtil;
import com.tbs.web.enums.TokenStatusEnum;
import com.tbs.web.security.issuance.center.ITokenIssuanceCenter;
import com.tbs.web.security.issuance.form.TokenRequestForm;
import com.tbs.web.security.token.ITokenModel;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * 无实现Token签发中心
 * @author abstergo
 */
public class NoImplTokenIssuanceCenter implements ITokenIssuanceCenter {

	public NoImplTokenIssuanceCenter() {
		AssertUtil.throwError(() -> new UnsupportedOperationException(I18nUtil.get("token.issuance.not.implemented")));
	}

	@Override
	public ITokenModel issueToken(TokenRequestForm form) {
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

    @Override
    public Collection<ITokenModel> getTokensBySessionId(String sessionId) {
        return List.of();
    }

}