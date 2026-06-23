package com.tbs.web.security.user.access.impl;

import com.tbs.common.error.AssertUtil;
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
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 刷新令牌提供者
 *
 * @author abstergo
 */
@Slf4j
public class DefaultRefreshAccessTokenProvider extends AbstractAccessTokenProvider<IRefreshTokenForm>
    implements IRefreshAccessTokenProvider {

    @Resource
    IUserInfoService userInfoService;

    @Resource
    IUserLoginInfoService loginInfoService;

    @Override
    protected void validateForm(@NotNull IRefreshTokenForm iRefreshTokenForm) {
        ITokenModel refreshToken = iRefreshTokenForm.getRefreshToken();
        AssertUtil.notNull(refreshToken, "刷新令牌为空");
        AssertUtil.hasText(refreshToken.getToken(), "刷新令牌内容为空");
        AssertUtil.hasText(refreshToken.getUserName(), "用户名不能为空");
        AssertUtil.isTrue(!Objects.equals(refreshToken.getStatus(), TokenStatusEnum.ACTIVE), "刷新令牌已失效");
        AssertUtil.isTrue(!Objects.equals(TokenTypeEnum.REFRESH_TOKEN, refreshToken.getTokenType()), "令牌类型异常");
    }

    @Override
    protected UserDetails pullUserDetails(@NotNull IRefreshTokenForm iRefreshTokenForm) {
        ITokenModel refreshToken = iRefreshTokenForm.getRefreshToken();
        UserDetails details = null;
        try {
            String oldToken = AuthUtil.getInstance().getCurrentTokenModel().getToken();
            details = loginInfoService.getUserDetails(oldToken);
            loginInfoService.removeUserLoginInfo(oldToken);
        } catch (Exception e) {
            log.warn("用户信息获取失败，将重建登录时用户信息");
            details = userInfoService.loadUserInfoByUniqueKey(refreshToken.getUserName());
        }
        return details;
    }

    @Override
    protected TokenRequestForm createTokenRequestForm(@NotNull IRefreshTokenForm iRefreshTokenForm,
        UserDetails details) {
        return new TokenRequestForm(iRefreshTokenForm.getRefreshToken().getSessionId(), details,
            TokenTypeEnum.WEB_ACCESS_TOKEN.getCode(), TokenCreateTypeEnum.REFRESH.getCode());
    }
}
