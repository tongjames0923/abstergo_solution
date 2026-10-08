package com.tbs.sdk.model;

import java.util.Map;
import lombok.Data;

/**
 * 登录方式元数据
 * <p>
 * 对应GET /public/user/login-grants 的数组项，描述服务端启用的授权方式及其公开配置。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class LoginGrantModel {

    /**
     * 授权类型标识（如password/wecom/third_party）
     */
    private String grantType;

    /**
     * 展示文案i18n key
     */
    private String labelKey;

    /**
     * 公开配置（如企微corpId/agentId，键值结构由服务端约定）
     */
    private Map<String, Object> publicOptions;
}
