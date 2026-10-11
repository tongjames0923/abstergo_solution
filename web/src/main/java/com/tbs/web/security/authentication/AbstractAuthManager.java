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

import jakarta.annotation.Resource;

/**
 * 抽象认证管理器
 * <p>
 * 模板实现 {@link IAuthManager} 的通用逻辑：基于 {@code getCurrentAuthentication()} 判定登录态，
 * 并提供「用户权限变更时按会话维度批量吊销令牌」的能力。具体认证上下文的读写由子类实现。
 * </p>
 * <p>
 * 依赖：令牌颁发中心（查询会话令牌）、登录信息服务（清理缓存）、令牌吊销器（使令牌失效）。
 * 使用场景：认证拦截器在用户权限/角色发生变更后调用 {@link #notifyUserAuthStatusChanged} 强制下线。
 * </p>
 *
 * @author abstergo
 * @see IAuthManager 认证管理器契约
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
        // 权限变更强下线：按用户名查出所有会话，逐会话吊销其令牌并清理登录缓存，使下次请求重新认证
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
                        // 单条令牌吊销失败不影响其他会话，记录后继续
                        log.warn("用户{}权限变更失败", userName, e);
                    }

                }
            }
        }
        log.info("用户{}权限变更完成", userName);
    }
}
