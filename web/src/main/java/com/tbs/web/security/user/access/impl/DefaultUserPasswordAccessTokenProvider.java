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
 * 默认用户名密码访问令牌提供者
 * <p>
 * 以「用户名 + 密码」换取访问令牌：校验表单非空，按用户名加载用户并比对加密后的密码，
 * 通过后为新会话签发访问令牌（Web 登录为 {@code WEB_ACCESS_TOKEN}，客户端登录为 {@code APPLICATION_ACCESS_TOKEN}）。
 * </p>
 *
 * @author abstergo
 * @see AbstractAccessTokenProvider 抽象基类
 */
public class DefaultUserPasswordAccessTokenProvider extends AbstractAccessTokenProvider<IUsernameAndPasswordForm>
    implements IUsernamePasswordAccessTokenProvider {

    @Resource
    private IUserInfoService userInfoService;

    @Resource
    IPasswordEncrypt encrypt;

    @Override
    protected void validateForm( IUsernameAndPasswordForm iUsernameAndPasswordForm) {
        AssertUtil.hasText(iUsernameAndPasswordForm.getUsername(), I18nUtil.get("user.name.required"));
        AssertUtil.hasText(iUsernameAndPasswordForm.getPassword(), I18nUtil.get("user.password.required"));
    }

    @Override
    protected UserDetails pullUserDetails( IUsernameAndPasswordForm iUsernameAndPasswordForm) {
        UserDetails details = userInfoService.loadUserInfoByUniqueKey(iUsernameAndPasswordForm.getUsername());
        AssertUtil.notNull(details, I18nUtil.get("user.not.found.with.username", iUsernameAndPasswordForm.getUsername()));
        // 明文密码经 IPasswordEncrypt 加密后与存储密文比对，不一致即密码错误
        AssertUtil.isTrue(
            !Objects.equals(encrypt.encrypt(iUsernameAndPasswordForm.getPassword()), details.getPassword()),
            I18nUtil.get("user.password.error.with.username", iUsernameAndPasswordForm.getUsername()));
        return details;
    }

    @Override
    protected TokenRequestForm createTokenRequestForm( IUsernameAndPasswordForm iUsernameAndPasswordForm,
        UserDetails details) {
        // 生成新会话标识；按登录类型区分令牌类型
        return new TokenRequestForm(UuidUtil.getInstance().getUuidString(), details,
            Objects.equals(1, iUsernameAndPasswordForm.getLoginType())
                ? TokenTypeEnum.APPLICATION_ACCESS_TOKEN.getCode() : TokenTypeEnum.WEB_ACCESS_TOKEN.getCode(),
            TokenCreateTypeEnum.LOGIN.getCode());
    }

}
