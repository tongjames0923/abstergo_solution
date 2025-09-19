package com.tbs.web.security.user.access;

import com.tbs.common.util.CollUtil;
import com.tbs.common.util.SpringUtil;
import com.tbs.web.enums.TokenCreateTypeEnum;
import com.tbs.web.enums.TokenTypeEnum;
import com.tbs.web.security.issuance.ITokenIssuanceCenter;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.user.IUserLoginInfoService;
import com.tbs.web.security.user.IUserValidator;
import com.tbs.web.uitl.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Collection;

/**
 * 抽象令牌提供者
 *
 * @author abstergo
 */
@Slf4j
public abstract class AbstractAccessTokenProvider<FORM extends Serializable> implements IUserAccessTokenProvider<FORM> {

    /**
     * 验证表单
     *
     * @param form 表单
     */
    protected abstract void validateForm(@NotNull FORM form);

    /**
     * 验证用户信息
     *
     * @param userDetails 用户信息
     */
    protected void validateUser(@NotNull UserDetails userDetails) {
        AuthUtil.checkUserDetails(userDetails);
        var validators = getUserValidators();
        if (CollUtil.isEmpty(validators)) {
            return;
        }
        getUserValidators().forEach(validator -> validator.validate(userDetails));
    }

    /**
     * 拉取用户信息
     *
     * @param form 表单
     * @return 用户信息
     */
    protected abstract UserDetails pullUserDetails(@NotNull FORM form);

    protected int getTokenCreateType() {
        return TokenCreateTypeEnum.LOGIN.getCode();
    }

    /**
     * 用户验证器
     *
     * @return 用户验证器
     */
    protected Collection<IUserValidator> getUserValidators() {
        try {
            return SpringUtil.getBeansOfType(IUserValidator.class).values();
        } catch (Exception e) {
            log.warn("用户验证器获取失败...");
            return new java.util.ArrayList<>();
        }
    }

    /**
     * 创建令牌
     *
     * @param tokenIssuanceCenter 令牌发放中心
     * @param userDetails         用户信息
     * @return 令牌
     */
    protected ITokenModel createToken(ITokenIssuanceCenter tokenIssuanceCenter, @NotNull UserDetails userDetails) {
        return tokenIssuanceCenter.issueToken(userDetails, TokenTypeEnum.ACCESS_TOKEN.getCode(), getTokenCreateType());
    }

    @Resource
    private IUserLoginInfoService userLoginInfoService;

    protected void afterCreateToken(ITokenModel token, UserDetails details) {
        userLoginInfoService.insertUserLoginInfo(details, token);
    }

    @Resource
    private ITokenIssuanceCenter tokenIssuanceCenter;

    @Override
    public ITokenModel requestToken(@NotNull FORM form) {
        validateForm(form);
        UserDetails userDetails = pullUserDetails(form);
        validateUser(userDetails);
        ITokenModel model = createToken(tokenIssuanceCenter, userDetails);
        afterCreateToken(model, userDetails);
        return model;
    }


}
