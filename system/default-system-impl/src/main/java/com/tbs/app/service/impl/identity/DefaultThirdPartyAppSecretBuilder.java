package com.tbs.app.service.impl.identity;

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
     * @param appEntity
     * @return
     */
    @Override
    public String build(ThirdPartyAppEntity appEntity) {
        AssertUtil.isTrueBusinessError(appEntity == null).i18n("third_party_app_secret_builder.app_entity_is_null")
            .throwIt();
        var appId = appEntity.getId();
        var proxyId = appEntity.getProxyId();
        var grantType = appEntity.getGrantType();
        long createTime = appEntity.getCreateTime().getTime();
        var baseSecret = "#" + createTime + "#" + proxyId + "#" + grantType + "#" + appId + "#" + SALT + "#";
        return EncryptUitl.hash256Encrypt(baseSecret, "");
    }
}
