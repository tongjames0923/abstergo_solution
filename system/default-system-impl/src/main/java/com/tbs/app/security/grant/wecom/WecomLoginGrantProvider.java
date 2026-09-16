package com.tbs.app.security.grant.wecom;

import com.tbs.app.constant.SystemI18nConstant;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.framework.UuidUtil;
import com.tbs.web.constant.WebI18nConstant;
import com.tbs.web.enums.TokenCreateTypeEnum;
import com.tbs.web.enums.TokenTypeEnum;
import com.tbs.web.security.issuance.form.TokenRequestForm;
import com.tbs.web.security.user.IUserInfoService;
import com.tbs.web.security.user.access.AbstractAccessTokenProvider;
import com.tbs.web.security.user.access.grant.ILoginGrantProvider;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashMap;
import java.util.Map;

/**
 * 企业微信登录授权提供者
 * <p>
 * 复用 {@link AbstractAccessTokenProvider} 模板链路（校验表单→拉取用户→校验链→签发→写登录信息），
 * 仅翻译「企业微信授权码 → 系统用户」：授权码换企微 userid，再按外部身份绑定关系加载系统用户。
 * 前端重定向（wecom-sso）与内嵌扫码（wecom-qr）两种交互均产出 code，统一走本授权类型。
 * </p>
 *
 * @author abstergo
 * @see ILoginGrantProvider 登录授权提供者 SPI
 */
public class WecomLoginGrantProvider extends AbstractAccessTokenProvider<WecomLoginForm>
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

    private final WecomApiClient apiClient;

    private final IUserInfoService userInfoService;

    /**
     * 构建企业微信登录授权提供者
     *
     * @param property       企业微信登录配置属性
     * @param apiClient      企业微信 API 客户端
     * @param userInfoService 用户信息服务（外部身份加载）
     */
    public WecomLoginGrantProvider(WecomProperty property, WecomApiClient apiClient, IUserInfoService userInfoService) {
        this.property = property;
        this.apiClient = apiClient;
        this.userInfoService = userInfoService;
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
    protected void validateForm(WecomLoginForm form) {
        AssertUtil.isTrue(StrUtil.isEmpty(form.getCode())).i18n(SystemI18nConstant.WECOM_CODE_REQUIRED)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
    }

    @Override
    protected UserDetails pullUserDetails(WecomLoginForm form) {
        String accessToken = apiClient.getAccessToken();
        String wecomUserId = apiClient.getUserIdByCode(accessToken, form.getCode());
        UserDetails details = userInfoService.loadUserInfoByExternalIdentity(IDENTITY_TYPE, wecomUserId);
        AssertUtil.isTrue(details == null).i18n(WebI18nConstant.LOGIN_GRANT_EXTERNAL_NOT_BOUND)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        return details;
    }

    @Override
    protected TokenRequestForm createTokenRequestForm(WecomLoginForm form, UserDetails details) {
        return new TokenRequestForm(UuidUtil.getInstance().getUuidString(), details,
            TokenTypeEnum.WEB_ACCESS_TOKEN.getCode(), TokenCreateTypeEnum.LOGIN.getCode());
    }
}
