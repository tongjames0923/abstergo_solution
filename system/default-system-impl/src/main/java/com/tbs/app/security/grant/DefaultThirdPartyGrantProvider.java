package com.tbs.app.security.grant;

import com.tbs.app.constant.SystemI18nConstant;
import com.tbs.app.security.form.DefaultThirdPartyLoginForm;
import com.tbs.app.service.identity.IThirdPartyAppService;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.encrypt.EncryptUitl;
import com.tbs.common.util.framework.UuidUtil;
import com.tbs.shared.enums.CommonFlagEnum;
import com.tbs.shared.enums.SystemUserStatusEnum;
import com.tbs.shared.pojo.criteria.CommonThirdPartyAppCriteria;
import com.tbs.shared.pojo.entity.ThirdPartyAppEntity;
import com.tbs.web.enums.TokenCreateTypeEnum;
import com.tbs.web.enums.TokenTypeEnum;
import com.tbs.web.security.issuance.center.ITokenIssuanceCenter;
import com.tbs.web.security.issuance.form.TokenRequestForm;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.user.IUserInfoService;
import com.tbs.web.security.user.access.grant.ILoginGrantProvider;
import org.springframework.context.annotation.Lazy;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 第三方应用登录授权提供方（{@code third_party} 授权类型）。
 * <p>
 * 作为统一登录门面的一种凭证授予实现，承载「第三方应用以应用标识 + 授权码」方式换取访问令牌的能力：
 * <ol>
 *     <li>依据请求中的应用标识定位已登记的第三方应用；</li>
 *     <li>校验第三方应用存在、其代理用户（执行人账号）存在且处于激活状态；</li>
 *     <li>以服务端约定算法 {@link #codeGenerate} 重现授权码，并与请求传入的授权码比对；</li>
 *     <li>校验通过后，以代理用户身份签发 {@code THIRD_PARTY_ACCESS_TOKEN} 类型令牌。</li>
 * </ol>
 * 授权类型标识 {@link #GRANT_TYPE} 全局唯一，需与前端登录方式 Provider id 对齐。
 * </p>
 *
 * @author abstergo
 */
public class DefaultThirdPartyGrantProvider implements ILoginGrantProvider<DefaultThirdPartyLoginForm> {

    @Lazy
    @Resource
    private IThirdPartyAppService thirdPartyAppService;

    @Lazy
    @Resource
    private ISystemUserService userService;

    @Resource
    private ITokenIssuanceCenter tokenService;

    @Resource
    private IUserInfoService userInfoService;

    /**
     * 授权码生成盐值（固定常量）。
     * <p>参与 {@link #codeGenerate} 的哈希原料拼接，用于提升授权码不可预测性，禁止对外暴露或运行时修改。</p>
     */
    public static final String SALT = "cad39065478d1e81fc528f9a6be211a5";

    /**
     * 依据第三方应用信息与时间戳，按约定算法生成授权码。
     * <p>
     * 原料拼接顺序为 {@code SALT : appSecret : timeStamp : appId}，经 SHA-256 哈希并转小写。 服务端以此重建授权码，与请求方传入的 code
     * 比对，从而在不暴露密钥的前提下完成身份校验。
     * </p>
     *
     * @param thirdPartyApp 第三方应用实体（提供 appId 与 appSecret）
     * @param timeStamp     请求携带的时间戳（与生成方保持一致，用于抗重放）
     * @return 32 位小写十六进制授权码字符串
     */
    public static final String codeGenerate(ThirdPartyAppEntity thirdPartyApp, Long timeStamp) {
        var sec = thirdPartyApp.getAppSecret();
        var finalBase = SALT + ":" + sec + ":" + String.valueOf(timeStamp) + ":" + thirdPartyApp.getAppId();
        return EncryptUitl.hash256Encrypt(finalBase, "").toLowerCase();
    }

    /**
     * 授权类型标识
     */
    public static final String GRANT_TYPE = "third_party";

    /**
     * 获取授权类型唯一标识
     * <p>
     * 全局唯一，重复注册在启动期即失败；与前端登录方式 Provider id 对齐。
     * </p>
     *
     * @return 授权类型标识（如 password / wecom）
     */
    @Override
    public String getGrantType() {
        return GRANT_TYPE;
    }

    /**
     * 获取凭证表单类型
     * <p>
     * 供统一门面将原始请求体反序列化为强类型表单。
     * </p>
     *
     * @return 凭证表单 Class
     */
    @Override
    public Class<DefaultThirdPartyLoginForm> getFormType() {
        return DefaultThirdPartyLoginForm.class;
    }

    /**
     * 当前授权方式是否启用
     * <p>
     * 建议绑定各自配置总开关（{@code tbs.{module}.enable}）；未启用方式在统一端点与元数据导出中均被拒绝。
     * </p>
     *
     * @return 启用返回 {@code true}
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * 请求访问令牌
     * <p>
     * 按具体凭证完成认证并签发访问令牌；凭证非法或校验不通过时由实现方抛异常。
     * </p>
     *
     * @param defaultThirdPartyLoginForm 换取令牌所需的请求表单，不能为空
     * @return 签发的访问令牌模型
     */
    @Override
    public ITokenModel requestToken(DefaultThirdPartyLoginForm defaultThirdPartyLoginForm) {
        AssertUtil.isTrueBusinessError(defaultThirdPartyLoginForm == null)
            .i18n(SystemI18nConstant.THIRD_PARTY_LOGIN_FORM_CANNOT_BE_NULL).throwIt();
        var thirdPartyApp = CollUtil.getFirst(thirdPartyAppService.query(
            CommonThirdPartyAppCriteria.builder().EqAppId(defaultThirdPartyLoginForm.getAppId()).build(), null));
        AssertUtil.isTrueBusinessError(thirdPartyApp == null).i18n(SystemI18nConstant.THIRD_PARTY_APP_NOT_EXISTS)
            .throwIt();
        AssertUtil.isTrueBusinessError(!Objects.equals(thirdPartyApp.getFlag(), CommonFlagEnum.ACTIVE.getCode()))
            .i18n(SystemI18nConstant.THIRD_PARTY_APP_NOT_ENABLED).throwIt();

        var user = userService.queryById(thirdPartyApp.getProxyId());

        AssertUtil.isTrueBusinessError(user == null).i18n(SystemI18nConstant.THIRD_PARTY_APP_PROXY_USER_NOT_EXISTS)
            .throwIt();
        AssertUtil.isTrueBusinessError(!Objects.equals(user.getFlag(), SystemUserStatusEnum.ACTIVE.getCode()))
            .i18n(SystemI18nConstant.THIRD_PARTY_APP_PROXY_USER_NOT_ACTIVE).throwIt();

        var code = defaultThirdPartyLoginForm.getCode();
        var gCode = codeGenerate(thirdPartyApp, defaultThirdPartyLoginForm.getTimestamp());
        AssertUtil.isTrueBusinessError(Objects.equals(gCode, code)).i18n(SystemI18nConstant.THIRD_PARTY_APP_CODE_ERROR)
            .throwIt();
        TokenRequestForm requestForm = new TokenRequestForm();
        requestForm.setTokenCreateType(TokenCreateTypeEnum.THIRD.getCode());
        requestForm.setSessionId(UuidUtil.getInstance().getUuidString());
        requestForm.setUserDetails(userInfoService.loadUserInfoByUniqueKey(user.getUserName()));
        requestForm.setTokenType(TokenTypeEnum.THIRD_PARTY_ACCESS_TOKEN.getCode());
        return tokenService.issueToken(requestForm);
    }
}
