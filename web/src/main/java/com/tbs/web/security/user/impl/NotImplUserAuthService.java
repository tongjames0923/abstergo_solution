package com.tbs.web.security.user.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.user.IUserAuthService;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 未实现用户登陆验证服务
 *
 * @author abstergo
 */
public class NotImplUserAuthService implements IUserAuthService {

    public NotImplUserAuthService() {
        AssertUtil.throwError(() -> new FormattedRuntimeException("未实现用户登陆验证服务"));
    }

    @Override
    public ITokenModel login(String username, String password) {
        return null;
    }

    @Override
    public void authToken(ITokenModel simpleTokenModel) throws AuthenticationException {

    }

    @Override
    public void submitToken(@NotNull ITokenModel simpleTokenModel, @NotNull UserDetails details) {

    }

    @Override
    public boolean logout(ITokenModel simpleTokenModel) {
        return false;
    }
}
