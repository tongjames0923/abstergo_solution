package com.tbs.web.security.authentication.center.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.i18n.I18nUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.web.exception.SimpleAuthException;
import com.tbs.web.security.authentication.center.AbstractAuthenticationCenter;
import com.tbs.web.security.authentication.model.DefaultTokenAuthentication;
import com.tbs.web.security.authentication.model.ITokenAuthentication;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.user.IUserInfoService;
import com.tbs.web.uitl.AuthUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.AuthenticationException;

import javax.annotation.Resource;

/**
 * 默认的认证中心
 *
 * @author jiangnan
 *
 */
public class SimpleAuthenticationCenter extends AbstractAuthenticationCenter {

    @Resource
    IUserInfoService userInfoService;

    @Override
    protected ITokenAuthentication createAuth( ITokenModel model) {
        DefaultTokenAuthentication tokenAuthentication = new DefaultTokenAuthentication(model, null);
        return tokenAuthentication;
    }

    @Override
    public ITokenAuthentication authenticate(ITokenAuthentication authentication) throws AuthenticationException {
        AssertUtil.isTrue(authentication == null, () -> new SimpleAuthException(I18nUtil.get("auth.info.empty")));
        AssertUtil.isTrue(authentication.getTokenModel() == null, () -> new SimpleAuthException(I18nUtil.get("auth.token.empty")));
        AssertUtil.isTrue(authentication.getUserDetails() == null, () -> new SimpleAuthException(I18nUtil.get("auth.user.empty")));
        try {
            AuthUtil.checkUserDetails(authentication.getUserDetails());
            authentication.setAuthenticated(true);
        } catch (Exception e) {
            authentication.setAuthenticated(false);
            AssertUtil.throwError(
                () -> new SimpleAuthException(StrUtil.format(I18nUtil.get("auth.user.verify.fail.detail"), e.getLocalizedMessage())));
        }
        return authentication;
    }
}
