package com.tbs.web.security.authentication.center.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.i18n.I18nUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.web.exception.SimpleAuthException;
import com.tbs.web.security.authentication.center.AbstractAuthenticationCenter;
import com.tbs.web.security.authentication.model.DefaultTokenAuthentication;
import com.tbs.web.security.authentication.model.ITokenAuthentication;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.user.IUserLoginInfoService;
import com.tbs.web.uitl.AuthUtil;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.annotation.Resource;
import com.tbs.web.constant.WebI18nConstant;

/**
 * 默认的认证中心
 * <p>
 * 基于 {@link IUserLoginInfoService} 装配用户详情：认证阶段一次性读取并注入认证对象，
 * 再对用户详情做有效性检查（如账号状态、绑定关系），全部通过则标记为已认证。
 * 注入后本次请求内的权限、用户名等读取均复用该快照，不再重复回源。
 * </p>
 *
 * @author jiangnan
 * @see AbstractAuthenticationCenter 抽象基类
 */
public class SimpleAuthenticationCenter extends AbstractAuthenticationCenter {

    @Resource
    IUserLoginInfoService userLoginInfoService;

    @Override
    protected ITokenAuthentication createAuth( ITokenModel model) {
        // 由令牌模型直接构造认证对象，异常信息暂置空，待 authenticate 阶段填充
        DefaultTokenAuthentication tokenAuthentication = new DefaultTokenAuthentication(model, null);
        return tokenAuthentication;
    }

    @Override
    public ITokenAuthentication authenticate(ITokenAuthentication authentication) throws AuthenticationException {
        AssertUtil.isTrue(authentication == null, () -> new SimpleAuthException(I18nUtil.get(com.tbs.web.constant.WebI18nConstant.AUTH_INFO_EMPTY)));
        ITokenModel tokenModel = authentication.getTokenModel();
        AssertUtil.isTrue(tokenModel == null, () -> new SimpleAuthException(I18nUtil.get(com.tbs.web.constant.WebI18nConstant.AUTH_TOKEN_EMPTY)));
        // 认证阶段一次性装配用户详情并注入认证对象，本次请求后续读取均复用该快照
        UserDetails userDetails = CollUtil.getFirst(userLoginInfoService.getUserDetails(tokenModel.getToken()));
        authentication.setUserDetails(userDetails);
        AssertUtil.isTrue(userDetails == null, () -> new SimpleAuthException(I18nUtil.get(com.tbs.web.constant.WebI18nConstant.AUTH_USER_EMPTY)));
        try {
            // 校验用户详情（状态、绑定等），通过则标记已认证
            AuthUtil.checkUserDetails(userDetails);
            authentication.setAuthenticated(true);
        } catch (Exception e) {
            authentication.setAuthenticated(false);
            AssertUtil.throwError(
                () -> new SimpleAuthException(StrUtil.format(I18nUtil.get(com.tbs.web.constant.WebI18nConstant.AUTH_USER_VERIFY_FAIL_DETAIL), e.getLocalizedMessage())));
        }
        return authentication;
    }
}
