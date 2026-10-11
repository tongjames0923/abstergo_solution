package com.tbs.web.security.user.access;

import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.web.security.issuance.center.ITokenIssuanceCenter;
import com.tbs.web.security.issuance.form.TokenRequestForm;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.user.IUserLoginInfoService;
import com.tbs.web.security.user.IUserValidator;
import com.tbs.web.uitl.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.annotation.Resource;
import java.io.Serializable;
import java.util.Collection;

/**
 * 抽象令牌提供者
 * <p>
 * 模板实现 {@link IUserAccessTokenProvider}：统一编排「校验表单 → 拉取用户 → 校验用户 → 签发令牌 → 写入登录信息」
 * 的访问令牌发放流程。具体凭证形态（用户名密码、刷新令牌等）由子类填充抽象方法。
 * </p>
 *
 * @param <FORM> 请求表单类型，需可序列化
 * @author abstergo
 * @see IUserAccessTokenProvider 访问令牌提供者契约
 */
@Slf4j
public abstract class AbstractAccessTokenProvider<FORM extends Serializable> implements IUserAccessTokenProvider<FORM> {

    /**
     * 校验请求表单的合法性
     *
     * @param form 请求表单，不能为空
     */
    protected abstract void validateForm( FORM form);

    /**
     * 校验用户详情
     * <p>
     * 先执行基础校验（账号状态等），再依次触发 Spring 容器中的 {@link IUserValidator} 校验链。
     * </p>
     *
     * @param userDetails 用户详情，不能为空
     */
    protected void validateUser( UserDetails userDetails) {
        AuthUtil.checkUserDetails(userDetails);
        var validators = getUserValidators();
        if (CollUtil.isEmpty(validators)) {
            return;
        }
        getUserValidators().forEach(validator -> validator.validate(userDetails));
    }

    /**
     * 从表单拉取用户详情
     *
     * @param form 请求表单，不能为空
     * @return 用户详情，不能为空
     */
    protected abstract UserDetails pullUserDetails( FORM form);

    /**
     * 收集容器中的用户验证器
     *
     * @return 用户验证器集合，获取失败时返回空集合（不阻断主流程）
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
     * 由表单与用户详情组装令牌请求表单
     *
     * @param form    请求表单，不能为空
     * @param details 用户详情，不能为空
     * @return 令牌请求表单
     */
    protected abstract TokenRequestForm createTokenRequestForm( FORM form, UserDetails details);

    @Resource
    private IUserLoginInfoService userLoginInfoService;

    /**
     * 令牌签发后的钩子
     * <p>
     * 将用户登录信息与令牌关联写入，供后续认证回源。
     * </p>
     *
     * @param token   已签发的令牌模型，不能为空
     * @param details 用户详情，不能为空
     */
    protected void afterCreateToken(ITokenModel token, UserDetails details) {
        userLoginInfoService.insertUserLoginInfo(details, token);
    }

    @Resource
    private ITokenIssuanceCenter tokenIssuanceCenter;

    @Override
    public ITokenModel requestToken( FORM form) {
        validateForm(form);
        UserDetails userDetails = pullUserDetails(form);
        validateUser(userDetails);
        ITokenModel model = tokenIssuanceCenter.issueToken(createTokenRequestForm(form, userDetails));
        afterCreateToken(model, userDetails);
        return model;
    }

}
