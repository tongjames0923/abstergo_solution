package com.tbs.web.security.issuance.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.i18n.I18nUtil;
import com.tbs.web.enums.TokenStatusEnum;
import com.tbs.web.security.issuance.center.ITokenIssuanceCenter;
import com.tbs.web.security.issuance.form.TokenRequestForm;
import com.tbs.web.security.token.ITokenModel;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 无实现 Token 签发中心
 * <p>
 * 占位实现：用于尚未接入具体签发中心的场景。构造即抛 {@link UnsupportedOperationException}，
 * 提醒使用者必须提供真实的 {@link ITokenIssuanceCenter} 实现。其余方法均为空操作/空集合，避免空指针。
 * </p>
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
	public ITokenModel getTokenByToken( String token) {
		return null;
	}

    @Override
    public void setTokenStatus( String token,  TokenStatusEnum status) {

    }

    @Override
    public Collection<ITokenModel> getTokensByUserNameAndTokenType(String userName, int tokenType) {
        return List.of();
    }

    @Override
    public Collection<ITokenModel> getTokensBySessionId(String sessionId) {
        return List.of();
    }

    @Override
    public Collection<ITokenModel> listSessions(String userName, Date beg, Date end) {
        return List.of();
    }

}
