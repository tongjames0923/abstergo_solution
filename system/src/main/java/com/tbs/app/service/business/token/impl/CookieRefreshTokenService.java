package com.tbs.app.service.business.token.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.web.enums.TokenCreateTypeEnum;
import com.tbs.web.enums.TokenTypeEnum;
import com.tbs.web.security.issuance.center.ITokenIssuanceCenter;
import com.tbs.web.security.issuance.form.TokenRequestForm;
import com.tbs.web.security.token.IRefreshTokenService;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.token.ITokenRemover;
import com.tbs.web.security.user.access.IRefreshAccessTokenProvider;
import com.tbs.web.security.user.form.IRefreshTokenForm;
import com.tbs.web.uitl.AuthUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 默认的Cookie刷新令牌服务实现
 *
 * @author abstergo
 */
@Slf4j
public class CookieRefreshTokenService implements IRefreshTokenService {

    @Resource
    private IRefreshAccessTokenProvider refreshAccessTokenProvider;

    @Resource
    private ITokenIssuanceCenter center;

    @Resource
    private HttpServletResponse response;

    @Resource
    private HttpServletRequest request;

    @Resource
    private ITokenRemover remover;

    private void pushRefreshToken(ITokenModel refreshToken) {
        Cookie cookie = new Cookie("REFRESH_TOKEN", refreshToken.getToken());
        cookie.setMaxAge(7 * 24 * 60 * 60);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private String getRefreshToken() {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("REFRESH_TOKEN".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    @Data
    private static class SimpleRefreshTokenForm implements IRefreshTokenForm {
        private ITokenModel refreshToken;
    }

    @Override
    public void inactiveRefreshToken() {
        String refreshToken = getRefreshToken();
        AssertUtil.hasText(refreshToken, "不存在刷新令牌");
        ITokenModel refreshTokenModel = center.getTokenByToken(refreshToken);
        AssertUtil.notNull(refreshTokenModel, "刷新令牌不存在");
        remover.inactiveToken(refreshTokenModel);
    }

    @Override
    public ITokenModel createRefreshToken() {
        ITokenModel currentTokenModel = AuthUtil.getInstance().getCurrentTokenModel();
        ITokenModel refreshToken = center.issueToken(
            new TokenRequestForm(currentTokenModel.getSessionId(), AuthUtil.getInstance().getCurrentUser(),
                TokenTypeEnum.REFRESH_TOKEN.getCode(), TokenCreateTypeEnum.REFRESH.getCode()));
        pushRefreshToken(refreshToken);
        return refreshToken;
    }

    @Override
    public ITokenModel useRefreshToken() {
        String refreshToken = getRefreshToken();
        AssertUtil.hasText(refreshToken, "不存在刷新令牌");
        ITokenModel refreshTokenModel = center.getTokenByToken(refreshToken);
        AssertUtil.notNull(refreshTokenModel, "刷新令牌不存在");
        SimpleRefreshTokenForm form = new SimpleRefreshTokenForm();
        form.setRefreshToken(refreshTokenModel);
        ITokenModel model = refreshAccessTokenProvider.requestToken(form);
        remover.inactiveToken(refreshTokenModel);
        return model;
    }
}
