package com.tbs.web.security.authentication.impl;

import com.tbs.web.enums.TokenTypeEnum;
import com.tbs.web.security.authentication.ITokenCleanner;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.user.IUserLoginInfoService;
import org.springframework.stereotype.Service;

/**
 * 用户信息缓存清理器
 *
 * @author tongj
 */
@Service
public class UserInfoCacheCleanner implements ITokenCleanner {

    private IUserLoginInfoService loginInfoService;

    public UserInfoCacheCleanner(IUserLoginInfoService loginInfoService) {
        this.loginInfoService = loginInfoService;
    }

    @Override
    public String getName() {
        return "用户登录信息清理器";
    }

    @Override
    public void clean(ITokenModel token) {
        // 如果是刷新令牌，则不清理登录缓存
        if (TokenTypeEnum.REFRESH_TOKEN.getCode().equals(token.getTokenType().getCode())) {
            return;
        }
        //其他的访问令牌
        loginInfoService.removeUserLoginInfo(token.getToken());
    }
}
