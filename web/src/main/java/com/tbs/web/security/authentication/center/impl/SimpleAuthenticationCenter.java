package com.tbs.web.security.authentication.center.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.i18n.I18nUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.web.exception.SimpleAuthException;
import com.tbs.web.security.authentication.center.AbstractAuthenticationCenter;
import com.tbs.web.security.authentication.model.DefaultTokenAuthentication;
import com.tbs.web.security.authentication.model.ITokenAuthentication;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.user.IUserInfoService;
import com.tbs.web.uitl.AuthUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.AuthenticationException;

import javax.annotation.Resource;

/**
 * 默认的认证中心
 * <p>
 * 基于 {@link IUserInfoService} 加载用户详情完成令牌认证：构造认证对象后校验其令牌模型、用户详情，
 * 再对用户详情做有效性检查（如账号状态、绑定关系），全部通过则标记为已认证。
 * </p>
 *
 * @author jiangnan
 * @see AbstractAuthenticationCenter 抽象基类
 */
public class SimpleAuthenticationCenter extends AbstractAuthenticationCenter {

    @Resource
    IUserInfoService userInfoService;

    @Override
    protected ITokenAuthentication createAuth( ITokenModel model) {
        // 由令牌模型直接构造认证对象，异常信息暂置空，待 authenticate 阶段填充
        DefaultTokenAuthentication tokenAuthentication = new DefaultTokenAuthentication(model, null);
        return tokenAuthentication;
    }

    @Override
    public ITokenAuthentication authenticate(ITokenAuthentication authentication) throws AuthenticationException {
        AssertUtil.isTrue(authentication == null, () -> new SimpleAuthException(I18nUtil.get("auth.info.empty")));
        AssertUtil.isTrue(authentication.getTokenModel() == null, () -> new SimpleAuthException(I18nUtil.get("auth.token.empty")));
        AssertUtil.isTrue(authentication.getUserDetails() == null, () -> new SimpleAuthException(I18nUtil.get("auth.user.empty")));
        try {
            // 校验用户详情（状态、绑定等），通过则标记已认证
            AuthUtil.checkUserDetails(authentication.getUserDetails());
            authentication.setAuthenticated(true);
        } catch (Exception e) {
            authentication.setAuthenticated(false);
            AssertUtil.throwError(
                () -> new SimpleAuthException(StrUtil.format(I18nUtil.get("auth.user.verify.fail.detail"), e.getLocalizedMessage())));
        }
        return authentication;
    }
}
