package com.tbs.app.service.impl.identity;

import com.tbs.app.constant.SystemI18nConstant;
import com.tbs.app.service.identity.IThirdPartyAppSecretBuilder;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.encrypt.EncryptUitl;
import com.tbs.shared.pojo.entity.ThirdPartyAppEntity;
import org.springframework.stereotype.Component;

@Component
public class DefaultThirdPartyAppSecretBuilder implements IThirdPartyAppSecretBuilder {

    private static final String SALT = "j8Z6yQpjcy4sHndwrX4e";

    /**
     * 构建第三方应用的密钥
     *
     * @param appEntity 待构建密钥的第三方应用实体
     * @return 基于约定原料拼接后经 SHA-256 哈希的应用密钥
     */
    @Override
    public String build(ThirdPartyAppEntity appEntity) {
        AssertUtil.isTrueBusinessError(appEntity == null)
            .i18n(SystemI18nConstant.THIRD_PARTY_APP_SECRET_BUILDER_ENTITY_NULL).throwIt();
        var appId = appEntity.getId();
        var proxyId = appEntity.getProxyId();
        var grantType = appEntity.getGrantType();
        long createTime = appEntity.getCreateTime().getTime();
        var baseSecret = "#" + createTime + "#" + proxyId + "#" + grantType + "#" + appId + "#" + SALT + "#";
        return EncryptUitl.hash256Encrypt(baseSecret, "");
    }
}
