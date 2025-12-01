package com.tbs.web.gate.center.impl;

import com.tbs.common.IStartup;
import com.tbs.common.error.AssertUtil;
import com.tbs.web.config.property.WebProperty;
import com.tbs.web.exception.NotFoundTokenException;
import com.tbs.web.gate.ITokenPicker;
import com.tbs.web.gate.center.AbstractAntPathSupportGateWayCenter;
import com.tbs.web.security.authentication.center.IAuthenticationCenter;
import com.tbs.web.security.authentication.model.ITokenAuthentication;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.uitl.AuthUtil;
import org.springframework.security.authentication.AuthenticationManager;

import javax.annotation.Resource;

/**
 * 抽象登录安全管理器
 *
 * @author abstergo
 */
public class LoginGateWayCenter extends AbstractAntPathSupportGateWayCenter implements IStartup {

    private ITokenPicker loginTokenPicker;

    @Resource
    IAuthenticationCenter authenticationCenter;

    @Resource
    private AuthenticationManager manager;

    @Resource
    WebProperty webProperty;

    public LoginGateWayCenter(ITokenPicker loginTokenPicker) {
        this.loginTokenPicker = loginTokenPicker;
    }

    @Override
    public ITokenPicker getTokenPicker() {
        return loginTokenPicker;
    }

    @Override
    public void onNoneToken() {
        AssertUtil.throwError(() -> new NotFoundTokenException("身份令牌不存在。"));
    }

    @Override
    public void handleToken(ITokenModel tokenModel) {
        ITokenAuthentication tokenAuthentication = authenticationCenter.createAuthenticationByTokenModel(tokenModel);
        AuthUtil.getInstance().setAuthentication((ITokenAuthentication)manager.authenticate(tokenAuthentication));
    }

    @Override
    public boolean onErrorWillBroken(Throwable e) {
        return true;
    }

    @Override
    public void startUp() throws RuntimeException {
        setMatcher(webProperty.getSecuredPaths());
    }

}
