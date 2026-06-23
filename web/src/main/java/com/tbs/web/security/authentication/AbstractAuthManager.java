package com.tbs.web.security.authentication;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.web.enums.TokenTypeEnum;
import com.tbs.web.exception.NoLoginException;
import com.tbs.web.security.issuance.center.ITokenIssuanceCenter;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.token.ITokenRemover;
import com.tbs.web.security.user.IUserLoginInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Resource;

/**
 * 抽象认证管理器
 *
 * @author abstergo
 */
@Slf4j
public abstract class AbstractAuthManager implements IAuthManager {

    @Override
    public boolean isUserLoggedIn() {
        return getCurrentAuthentication() != null && getCurrentAuthentication().isAuthenticated();
    }

    @Override
    public UserDetails getCurrentUser() {
        AssertUtil.isTrue(!isUserLoggedIn(), () -> new NoLoginException("当前用户未登录"));
        return getCurrentAuthentication().getUserDetails();
    }

    @Override
    public ITokenModel getCurrentTokenModel() {
        AssertUtil.isTrue(!isUserLoggedIn(), () -> new NoLoginException("当前用户未登录"));
        return getCurrentAuthentication().getTokenModel();
    }

    @Resource
    private ITokenIssuanceCenter tokenIssuanceCenter;
    @Resource
    private IUserLoginInfoService loginInfoService;
    @Resource
    private ITokenRemover remover;

    public ITokenIssuanceCenter getTokenIssuanceCenter() {
        return tokenIssuanceCenter;
    }

    public IUserLoginInfoService getLoginInfoService() {
        return loginInfoService;
    }

    public ITokenRemover getRemover() {
        return remover;
    }

    @Override
    public void notifyUserAuthStatusChanged(String userName) {
        log.info("用户权限发生变更，用户名:{}", userName);
        var sessionIds = loginInfoService.getUserAvaliableSessionId(userName);
        if (CollUtil.isNotEmpty(sessionIds)) {
            for (var sessionId : sessionIds) {
                if (StrUtil.isEmpty(sessionId)) {
                    continue;
                }
                var tokens = tokenIssuanceCenter.getTokensBySessionId(sessionId);
                if (CollUtil.isEmpty(tokens)) {
                    continue;
                }
                for (var token : tokens) {
                    try {
                        remover.revokeToken(token);
                        if (TokenTypeEnum.WEB_ACCESS_TOKEN.getCode().equals(token.getTokenType().getCode())) {
                            loginInfoService.removeUserLoginInfo(token.getToken());
                        }
                    } catch (Exception e) {
                        log.warn("用户{}权限变更失败", userName, e);
                    }

                }
            }
        }
        log.info("用户{}权限变更完成", userName);
    }
}
