package com.tbs.web.security.user.access.impl;

import com.tbs.common.error.AssertUtil;
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
        AssertUtil.hasText(iUsernameAndPasswordForm.getUsername(), "用户名不能为空");
        AssertUtil.hasText(iUsernameAndPasswordForm.getPassword(), "密码不能为空");
    }

    @Override
    protected UserDetails pullUserDetails(@NotNull IUsernameAndPasswordForm iUsernameAndPasswordForm) {
        UserDetails details = userInfoService.loadUserInfoByUniqueKey(iUsernameAndPasswordForm.getUsername());
        AssertUtil.notNull(details, "用户 {} 不存在");
        AssertUtil.isTrue(
            !Objects.equals(encrypt.encrypt(iUsernameAndPasswordForm.getPassword()), details.getPassword()),
            "用户 {} 密码错误");
        return details;
    }

    @Override
    protected TokenRequestForm createTokenRequestForm(@NotNull IUsernameAndPasswordForm iUsernameAndPasswordForm,
        UserDetails details) {
        return new TokenRequestForm(UuidUtil.getInstance().getUuidString(), details,
            TokenTypeEnum.ACCESS_TOKEN.getCode(), TokenCreateTypeEnum.LOGIN.getCode());
    }

}
