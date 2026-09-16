package com.tbs.app.service.identity;

import java.util.Map;

/**
 * 外部身份凭证换取 SPI
 * <p>
 * 平台差异的收敛点：将平台凭证（如企微 OAuth 授权码）换取为统一的
 * {@link ExternalUserProfile}；登录链路与自助绑定链路共用同一换取实现。
 * 新增第三方平台需提供本接口实现并注册为 Spring Bean。
 * </p>
 *
 * @author abstergo
 */
public interface IExternalIdentityExchanger {

    /**
     * 获取适用的外部身份类型
     *
     * @return 外部身份类型（如 wecom），与绑定表 identity_type 一致
     */
    String getIdentityType();

    /**
     * 按平台凭证换取外部用户信息
     *
     * @param credential 平台凭证键值（如 { code: 企微授权码 }），由各平台表单契约约定
     * @return 换取的外部用户信息载体（至少包含 externalId）
     */
    ExternalUserProfile exchange(Map<String, Object> credential);
}
