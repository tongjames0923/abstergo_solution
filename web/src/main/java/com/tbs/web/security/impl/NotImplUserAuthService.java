package com.tbs.web.security.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.web.security.ITokenModel;
import com.tbs.web.security.IUserAuthService;
import org.springframework.security.core.AuthenticationException;

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
    public boolean logout(ITokenModel simpleTokenModel) {
        return false;
    }
}
