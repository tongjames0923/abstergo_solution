package com.tbs.app.security.grant.wecom;

import com.tbs.app.constant.SystemI18nConstant;
import com.tbs.app.service.identity.ExternalUserProfile;
import com.tbs.app.service.identity.IExternalIdentityExchanger;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.StrUtil;

import java.util.Map;

/**
 * 企业微信凭证换取实现
 * <p>
 * 将企微 OAuth 授权码换取为统一的 {@link ExternalUserProfile}（externalId 即企微 userid）；
 * 登录链路（WecomLoginGrantProvider）与自助绑定链路共用本实现。
 * </p>
 *
 * @author abstergo
 * @see IExternalIdentityExchanger 平台凭证换取 SPI
 */
public class WecomIdentityExchanger implements IExternalIdentityExchanger {

    private final WecomApiClient apiClient;

    /**
     * 构建企微凭证换取实现
     *
     * @param apiClient 企业微信 API 客户端
     */
    public WecomIdentityExchanger(WecomApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public String getIdentityType() {
        return WecomLoginGrantProvider.IDENTITY_TYPE;
    }

    @Override
    public ExternalUserProfile exchange(Map<String, Object> credential) {
        Object code = credential == null ? null : credential.get("code");
        AssertUtil.isTrue(code == null || StrUtil.isEmpty(String.valueOf(code)))
            .i18n(SystemI18nConstant.WECOM_CODE_REQUIRED).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        String accessToken = apiClient.getAccessToken();
        String wecomUserId = apiClient.getUserIdByCode(accessToken, String.valueOf(code));
        ExternalUserProfile profile = new ExternalUserProfile();
        profile.setIdentityType(getIdentityType());
        profile.setExternalId(wecomUserId);
        return profile;
    }
}
