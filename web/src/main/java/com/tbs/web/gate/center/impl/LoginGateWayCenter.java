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
 * 登录网关中心
 * <p>
 * 针对配置的受保护路径（{@code WebProperty.securedPaths}）做登录态认证：提取令牌→构造认证对象→
 * 经 Spring Security {@link AuthenticationManager} 校验→写入认证上下文。无令牌或校验失败均直接中断请求。
 * </p>
 *
 * @author abstergo
 * @see AbstractAntPathSupportGateWayCenter 路径匹配基类
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
        // 受保护路径但无令牌：直接抛异常中断，由过滤器写 401
        AssertUtil.throwError(() -> new NotFoundTokenException("身份令牌不存在。"));
    }

    @Override
    public void handleToken(ITokenModel tokenModel) {
        // 构造认证对象并经 AuthenticationManager 校验，通过后写入当前线程认证上下文
        ITokenAuthentication tokenAuthentication = authenticationCenter.createAuthenticationByTokenModel(tokenModel);
        AuthUtil.getInstance().setAuthentication((ITokenAuthentication)manager.authenticate(tokenAuthentication));
    }

    @Override
    public boolean onErrorWillBroken(Throwable e) {
        // 登录相关异常为紧急异常，一律中断请求
        return true;
    }

    @Override
    public void startUp() throws RuntimeException {
        // 启动时加载受保护路径到路径匹配器
        setMatcher(webProperty.getSecuredPaths());
    }

}
