package com.tbs.web.security.user.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.web.security.token.AbstractAccessTokenProvider;
import com.tbs.web.security.user.IPasswordEncrypt;
import com.tbs.web.security.user.IUserInfoService;
import com.tbs.web.security.token.IUsernamePasswordAccessTokenProvider;
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

}
