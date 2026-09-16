package com.tbs.app.service.identity;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * 第三方用户信息载体
 * <p>
 * 外部身份的统一用户信息模型：由平台凭证换取（如企微授权码换 userid），
 * 供自动关联策略匹配与绑定管理消费；平台特有字段收纳于 {@code raw}。
 * </p>
 *
 * @author abstergo
 */
@Data
public class ExternalUserProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 外部身份类型（如 wecom），与绑定表 identity_type 一致
     */
    private String identityType;

    /**
     * 外部身份标识（如企微 userid）
     */
    private String externalId;

    /**
     * 第三方平台展示名（如企微成员姓名），可为空
     */
    private String displayName;

    /**
     * 平台原始信息（键值形态），可为空
     */
    private Map<String, Object> raw = Collections.emptyMap();
}
