package com.tbs.web.security.impl;

import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.util.AssertUtil;
import com.tbs.web.security.IUserAuthService;
import com.tbs.web.security.model.TokenModel;
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
    public UserDetails loadUserInfoByUniqueKey(String uniqueKey) {
        return null;
    }

    @Override
    public UserDetails loadUserInfoByToken(TokenModel token) {
        return null;
    }

    @Override
    public TokenModel login(String username, String password) {
        return null;
    }

    @Override
    public void authToken(TokenModel tokenModel) throws AuthenticationException {

    }

    @Override
    public boolean logout(TokenModel tokenModel) {
        return false;
    }
}
