package com.tbs.web.security.user.access.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.web.enums.TokenCreateTypeEnum;
import com.tbs.web.enums.TokenStatusEnum;
import com.tbs.web.enums.TokenTypeEnum;
import com.tbs.web.security.issuance.form.TokenRequestForm;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.user.IUserInfoService;
import com.tbs.web.security.user.IUserLoginInfoService;
import com.tbs.web.security.user.access.AbstractAccessTokenProvider;
import com.tbs.web.security.user.access.IRefreshAccessTokenProvider;
import com.tbs.web.security.user.form.IRefreshTokenForm;
import com.tbs.web.uitl.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 刷新令牌提供者（默认实现）
 * <p>
 * 由刷新令牌换取新的 Web 访问令牌：校验刷新令牌有效性与类型，复用当前会话或重建用户详情，
 * 再以 {@code REFRESH} 创建方式签发访问令牌。
 * </p>
 *
 * @author abstergo
 * @see AbstractAccessTokenProvider 抽象基类
 */
@Slf4j
public class DefaultRefreshAccessTokenProvider extends AbstractAccessTokenProvider<IRefreshTokenForm>
    implements IRefreshAccessTokenProvider {

    @Resource
    IUserInfoService userInfoService;

    @Resource
    IUserLoginInfoService loginInfoService;

    @Override
    protected void validateForm( IRefreshTokenForm iRefreshTokenForm) {
        ITokenModel refreshToken = iRefreshTokenForm.getRefreshToken();
        AssertUtil.notNull(refreshToken, "刷新令牌为空");
        AssertUtil.hasText(refreshToken.getToken(), "刷新令牌内容为空");
        AssertUtil.hasText(refreshToken.getUserName(), "用户名不能为空");
        AssertUtil.isTrue(!Objects.equals(refreshToken.getStatus(), TokenStatusEnum.ACTIVE), "刷新令牌已失效");
        AssertUtil.isTrue(!Objects.equals(TokenTypeEnum.REFRESH_TOKEN, refreshToken.getTokenType()), "令牌类型异常");
    }

    @Override
    protected UserDetails pullUserDetails( IRefreshTokenForm iRefreshTokenForm) {
        ITokenModel refreshToken = iRefreshTokenForm.getRefreshToken();
        UserDetails details = null;
        try {
            // 优先复用当前会话的登录信息，并清理旧登录缓存避免堆积
            String oldToken = AuthUtil.getInstance().getCurrentTokenModel().getToken();
            details = CollUtil.getFirst(loginInfoService.getUserDetails(oldToken));
            loginInfoService.removeUserLoginInfo(oldToken);
        } catch (Exception e) {
            // 回源失败时降级为按用户名重建用户详情
            log.warn("用户信息获取失败，将重建登录时用户信息");
            details = userInfoService.loadUserInfoByUniqueKey(refreshToken.getUserName());
        }
        return details;
    }

    @Override
    protected TokenRequestForm createTokenRequestForm( IRefreshTokenForm iRefreshTokenForm,
        UserDetails details) {
        // 沿用原会话标识，以 REFRESH 创建方式签发新的访问令牌
        return new TokenRequestForm(iRefreshTokenForm.getRefreshToken().getSessionId(), details,
            TokenTypeEnum.WEB_ACCESS_TOKEN.getCode(), TokenCreateTypeEnum.REFRESH.getCode());
    }
}
