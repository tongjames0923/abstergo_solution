package com.tbs.web.security.token.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.CollUtil;
import com.tbs.web.enums.TokenCreateTypeEnum;
import com.tbs.web.enums.TokenStatusEnum;
import com.tbs.web.enums.TokenTypeEnum;
import com.tbs.web.security.issuance.ITokenIssuanceCenter;
import com.tbs.web.security.token.IRefreshTokenService;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.user.IUserInfoService;
import com.tbs.web.security.user.IUserLoginInfoService;
import com.tbs.web.uitl.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * 默认的Cookie刷新令牌服务实现
 *
 * @author abstergo
 */
@Slf4j
public class CookieRefreshTokenService implements IRefreshTokenService {

    @Resource
    ITokenIssuanceCenter tokenIssuanceCenter;

    @Resource
    IUserInfoService userInfoService;

    @Resource
    IUserLoginInfoService loginInfoService;

    @Override
    public void pushRefreshToken(UserDetails userDetails, HttpServletResponse response,
        TokenCreateTypeEnum createTypeEnum) {
        ITokenModel token = tokenIssuanceCenter.issueToken(userDetails, TokenTypeEnum.REFRESH_TOKEN.getCode(),
            createTypeEnum.getCode());
        Cookie cookie = new Cookie("REFRESH_TOKEN", token.getToken());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    @Override
    public void removeRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (CollUtil.isEmpty(cookies)) {
            return;
        }
        for (Cookie cookie : cookies) {
            if ("REFRESH_TOKEN".equals(cookie.getName())) {
                tokenIssuanceCenter.setTokenStatus(cookie.getValue(), TokenStatusEnum.REVOKE);
                cookie.setValue(null);
                cookie.setMaxAge(0);
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                response.addCookie(cookie);
                return;
            }
        }
    }

    @Override
    public ITokenModel getRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (CollUtil.isEmpty(cookies)) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if ("REFRESH_TOKEN".equals(cookie.getName())) {
                return tokenIssuanceCenter.getTokenByToken(cookie.getValue());
            }
        }
        return null;
    }

    @Override
    public ITokenModel refreshAccessToken(HttpServletRequest request) {
        ITokenModel tokenModel = getRefreshToken(request);
        AssertUtil.notNull(tokenModel, "未查询到有效的刷新令牌数据");
        AssertUtil.isTrue(!Objects.equals(TokenTypeEnum.REFRESH_TOKEN, tokenModel.getTokenType()), "令牌类型异常");
        AssertUtil.isTrue(!Objects.equals(tokenModel.getStatus(), TokenStatusEnum.ACTIVE), "刷新令牌已失效");
        UserDetails details = null;
        try {
            String oldToken = AuthUtil.getInstance().getCurrentTokenModel().getToken();
            details = loginInfoService.getUserDetails(oldToken);
            loginInfoService.removeUserLoginInfo(oldToken);
        } catch (Exception e) {
            log.warn("用户信息获取失败，将重建登录时用户信息");
            details = userInfoService.loadUserInfoByUniqueKey(tokenModel.getUserName());
        }
        AuthUtil.checkUserDetails(details);
        AssertUtil.isTrue(!Objects.equals(details.getUsername(), tokenModel.getUserName()), "用户信息不一致");
        ITokenModel accessToken = tokenIssuanceCenter.issueToken(details, TokenTypeEnum.ACCESS_TOKEN.getCode(),
            TokenCreateTypeEnum.REFRESH.getCode());
        loginInfoService.insertUserLoginInfo(details, accessToken);
        return accessToken;
    }
}
