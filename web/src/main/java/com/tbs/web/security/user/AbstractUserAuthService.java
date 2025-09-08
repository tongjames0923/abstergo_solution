package com.tbs.web.security.user;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.web.enums.TokenCreateTypeEnum;
import com.tbs.web.enums.TokenStatusEnum;
import com.tbs.web.enums.TokenTypeEnum;
import com.tbs.web.security.issuance.center.ITokenIssuanceCenter;
import com.tbs.web.security.token.IRefreshTokenService;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.uitl.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * @author abstergo
 */

@Slf4j
public abstract class AbstractUserAuthService implements IUserAuthService {

    public static final String USER_DETAILS_CACHE = "user_details";

    @Resource
    ITokenIssuanceCenter tokenIssuanceCenter;

    @Resource
    IUserInfoService userInfoService;

    @Resource
    IRefreshTokenService refreshTokenService;

    /**
     * 验证用户名密码
     *
     * @param username
     * @param password
     * @return
     */
    protected abstract boolean validate(String username, String password);

    @Resource
    HttpServletResponse response;

    @Override
    public ITokenModel login(String username, String password) {
        boolean validate = validate(username, password);
        AssertUtil.isTrue(!validate, () -> new FormattedRuntimeException("用户名或密码错误"));
        UserDetails details = userInfoService.loadUserInfoByUniqueKey(username);
        AuthUtil.checkUserDetails(details);
        var token = tokenIssuanceCenter.issueToken(details, TokenTypeEnum.ACCESS_TOKEN.getCode(),
            TokenCreateTypeEnum.LOGIN.getCode());
        refreshTokenService.pushRefreshToken(details, response, TokenCreateTypeEnum.LOGIN);
        return token;
    }

    @Override
    public ITokenModel revokeToken(String token) {
        ITokenModel tokenModel = tokenIssuanceCenter.getTokenByToken(token);
        if (tokenModel == null) {
            return null;
        }
        if (TokenStatusEnum.ACTIVE.equals(tokenModel.getStatus())) {
            tokenIssuanceCenter.setTokenStatus(token, TokenStatusEnum.REVOKE);
            tokenModel.setStatus(TokenStatusEnum.REVOKE);
        }
        return tokenModel;
    }

    @Override
    public ITokenModel inActiveToken(String token) {
        ITokenModel tokenModel = tokenIssuanceCenter.getTokenByToken(token);
        if (tokenModel == null) {
            return null;
        }
        if (TokenStatusEnum.ACTIVE.equals(tokenModel.getStatus())) {
            tokenIssuanceCenter.setTokenStatus(token, TokenStatusEnum.INACTIVE);
            tokenModel.setStatus(TokenStatusEnum.INACTIVE);
        }
        return tokenModel;
    }

    @Async
    @Override
    @CacheEvict(cacheNames = USER_DETAILS_CACHE, key = "#userName")
    public void notifyUserAuthStatusChanged(String userName) {
        log.info("用户权限发生变更，用户名:{}", userName);

        for (ITokenModel tokenModel : tokenIssuanceCenter.getTokensByUserNameAndTokenType(userName,
            TokenTypeEnum.ACCESS_TOKEN.getCode())) {
            if (TokenStatusEnum.ACTIVE.getCode().equals(tokenModel.getStatus().getCode())) {
                tokenIssuanceCenter.setTokenStatus(tokenModel.getToken(), TokenStatusEnum.REVOKE);
            }
        }
        for (ITokenModel tokenModel : tokenIssuanceCenter.getTokensByUserNameAndTokenType(userName,
            TokenTypeEnum.REFRESH_TOKEN.getCode())) {
            if (TokenStatusEnum.ACTIVE.getCode().equals(tokenModel.getStatus().getCode())) {
                tokenIssuanceCenter.setTokenStatus(tokenModel.getToken(), TokenStatusEnum.REVOKE);
            }
        }
        log.info("用户{}权限变更完成", userName);
    }
}
