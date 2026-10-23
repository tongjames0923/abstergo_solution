package com.tbs.app.security.grant.wecom;

import com.tbs.app.constant.SystemI18nConstant;
import com.tbs.app.security.form.WecomLoginForm;
import com.tbs.app.security.grant.AbstractExternalLoginGrantProvider;
import com.tbs.app.service.identity.ExternalUserProfile;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.framework.UuidUtil;
import com.tbs.web.enums.TokenCreateTypeEnum;
import com.tbs.web.enums.TokenTypeEnum;
import com.tbs.web.security.issuance.form.TokenRequestForm;
import com.tbs.web.security.user.access.grant.ILoginGrantProvider;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashMap;
import java.util.Map;

/**
 * 企业微信登录授权提供者
 * <p>
 * 继承 {@link AbstractExternalLoginGrantProvider} 模板：绑定查询、未绑定自动关联、
 * 用户加载与令牌签发链路均由模板与既有模板复用；本类仅保留企微平台差异——
 * 授权类型自描述与「授权码 → 外部用户信息」换取（委托 {@link WecomIdentityExchanger}，
 * 与自助绑定链路共用）。
 * </p>
 *
 * @author abstergo
 * @see ILoginGrantProvider 登录授权提供者 SPI
 */
public class WecomLoginGrantProvider extends AbstractExternalLoginGrantProvider<WecomLoginForm>
    implements ILoginGrantProvider<WecomLoginForm> {

    /**
     * 企业微信授权类型标识（前端两种交互 Provider 共用本凭证类型）
     */
    public static final String GRANT_TYPE = "wecom";

    /**
     * 外部身份类型标识（外部身份绑定表 identity_type 取值）
     */
    public static final String IDENTITY_TYPE = "wecom";

    private final WecomProperty property;

    private final WecomIdentityExchanger identityExchanger;

    /**
     * 构建企业微信登录授权提供者
     *
     * @param property         企业微信登录配置属性
     * @param identityExchanger 企微凭证换取实现（与自助绑定链路共用）
     */
    public WecomLoginGrantProvider(WecomProperty property, WecomIdentityExchanger identityExchanger) {
        this.property = property;
        this.identityExchanger = identityExchanger;
    }

    @Override
    public String getGrantType() {
        return GRANT_TYPE;
    }

    @Override
    public Class<WecomLoginForm> getFormType() {
        return WecomLoginForm.class;
    }

    @Override
    public boolean isEnabled() {
        return property.isEnable();
    }

    @Override
    public Map<String, Object> getPublicOptions() {
        Map<String, Object> options = new HashMap<>(4);
        options.put("corpId", property.getCorpId());
        options.put("agentId", property.getAgentId());
        return options;
    }

    @Override
    public String getIdentityType() {
        return IDENTITY_TYPE;
    }

    @Override
    protected ExternalUserProfile pullExternalIdentity(WecomLoginForm form) {
        Map<String, Object> credential = new HashMap<>(2);
        credential.put("code", form.getCode());
        return identityExchanger.exchange(credential);
    }

    @Override
    protected void validateForm(WecomLoginForm form) {
        AssertUtil.isTrue(StrUtil.isEmpty(form.getCode())).i18n(SystemI18nConstant.WECOM_CODE_REQUIRED)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
    }

    @Override
    protected TokenRequestForm createTokenRequestForm(WecomLoginForm form, UserDetails details) {
        return new TokenRequestForm(UuidUtil.getInstance().getUuidString(), details,
            TokenTypeEnum.WEB_ACCESS_TOKEN.getCode(), TokenCreateTypeEnum.LOGIN.getCode());
    }
}
