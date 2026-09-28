package com.tbs.app.service.identity;

import com.tbs.shared.pojo.entity.ThirdPartyAppEntity;

/**
 * 第三方应用密钥构建器
 */
public interface IThirdPartyAppSecretBuilder {
    /**
     * 构建第三方应用的密钥
     *
     * @param appEntity
     * @return
     */
    String build(ThirdPartyAppEntity appEntity);

}
