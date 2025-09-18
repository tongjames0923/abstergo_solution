package com.tbs.web.security.user;

import com.tbs.common.IStartup;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.util.SpringUtil;
import com.tbs.web.enums.TokenCreateTypeEnum;
import com.tbs.web.enums.TokenStatusEnum;
import com.tbs.web.enums.TokenTypeEnum;
import com.tbs.web.security.issuance.ITokenIssuanceCenter;
import com.tbs.web.security.token.IRefreshTokenService;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.uitl.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author abstergo
 */

@Slf4j
public abstract class AbstractUserAuthService implements IUserAuthService, IStartup {

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

    @Resource
    HttpServletRequest request;

    @Resource
    IUserLoginInfoService userLoginInfoService;

    private static AbstractUserAuthService instance;

    List<IUserValidator> userValidators;

    @Resource
    private IPasswordEncrypt passwordEncrypt;

    public AbstractUserAuthService() {
        instance = this;
    }

    @Override
    public void startUp() throws RuntimeException {
        log.info("获取用户登录验证器...");
        try {
            userValidators = SpringUtil.getBeansOfType(IUserValidator.class).values().stream()
                .sorted((o1, o2) -> o1.getOrder() - o2.getOrder()).collect(Collectors.toList());
        } catch (Exception e) {
            userValidators = new java.util.ArrayList<>();
        }

        log.info("用户登录验证器数量: {}", userValidators.size());
    }

    @Override
    public ITokenModel login(String username, String password) {
        UserDetails details = userInfoService.loadUserInfoByUniqueKey(username);
        AuthUtil.checkUserDetails(details);
        password = passwordEncrypt.encrypt(password);
        AssertUtil.isTrue(!Objects.equals(details.getPassword(), password), () -> {
            return new FormattedRuntimeException("用户名或密码错误");
        });
        userValidators.forEach(validator -> validator.validate(details));
        var token = tokenIssuanceCenter.issueToken(details, TokenTypeEnum.ACCESS_TOKEN.getCode(),
            TokenCreateTypeEnum.LOGIN.getCode());
        userLoginInfoService.insertUserLoginInfo(details, token);
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
            userLoginInfoService.removeUserLoginInfo(token);
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
            userLoginInfoService.removeUserLoginInfo(token);
        }
        return tokenModel;
    }

    @Override
    public void logout(ITokenModel token) {
        AssertUtil.isTrue(token == null, () -> new FormattedRuntimeException("用户令牌为空，获取失败"));
        instance.inActiveToken(token.getToken());
        refreshTokenService.removeRefreshToken(request, response);
        userLoginInfoService.removeUserLoginInfo(token.getToken());
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
                try {
                    userLoginInfoService.removeUserLoginInfo(tokenModel.getToken());
                } catch (Exception e) {
                    log.warn("remove user login info error", e);
                }
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
