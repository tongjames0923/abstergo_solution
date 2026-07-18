package com.tbs.web.security.user.access.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.i18n.I18nUtil;
import com.tbs.common.util.framework.UuidUtil;
import com.tbs.web.enums.TokenCreateTypeEnum;
import com.tbs.web.enums.TokenTypeEnum;
import com.tbs.web.security.issuance.form.TokenRequestForm;
import com.tbs.web.security.user.IPasswordEncrypt;
import com.tbs.web.security.user.IUserInfoService;
import com.tbs.web.security.user.access.AbstractAccessTokenProvider;
import com.tbs.web.security.user.access.IUsernamePasswordAccessTokenProvider;
import com.tbs.web.security.user.form.IUsernameAndPasswordForm;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 默认用户密码访问令牌提供者
 *
 * @author abstergo
 */
public class DefaultUserPasswordAccessTokenProvider extends AbstractAccessTokenProvider<IUsernameAndPasswordForm>
    implements IUsernamePasswordAccessTokenProvider {

    @Resource
    private IUserInfoService userInfoService;

    @Resource
    IPasswordEncrypt encrypt;

    @Override
    protected void validateForm(@NotNull IUsernameAndPasswordForm iUsernameAndPasswordForm) {
        AssertUtil.hasText(iUsernameAndPasswordForm.getUsername(), I18nUtil.get("user.name.required"));
        AssertUtil.hasText(iUsernameAndPasswordForm.getPassword(), I18nUtil.get("user.password.required"));
    }

    @Override
    protected UserDetails pullUserDetails(@NotNull IUsernameAndPasswordForm iUsernameAndPasswordForm) {
        UserDetails details = userInfoService.loadUserInfoByUniqueKey(iUsernameAndPasswordForm.getUsername());
        AssertUtil.notNull(details, I18nUtil.get("user.not.found.with.username", iUsernameAndPasswordForm.getUsername()));
        AssertUtil.isTrue(
            !Objects.equals(encrypt.encrypt(iUsernameAndPasswordForm.getPassword()), details.getPassword()),
            I18nUtil.get("user.password.error.with.username", iUsernameAndPasswordForm.getUsername()));
        return details;
    }

    @Override
    protected TokenRequestForm createTokenRequestForm(@NotNull IUsernameAndPasswordForm iUsernameAndPasswordForm,
        UserDetails details) {
        return new TokenRequestForm(UuidUtil.getInstance().getUuidString(), details,
            Objects.equals(1, iUsernameAndPasswordForm.getLoginType())
                ? TokenTypeEnum.APPLICATION_ACCESS_TOKEN.getCode() : TokenTypeEnum.WEB_ACCESS_TOKEN.getCode(),
            TokenCreateTypeEnum.LOGIN.getCode());
    }

}