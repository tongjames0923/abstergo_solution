package com.tbs.app.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 外部身份手工绑定表单
 * <p>
 * 管理端按登录用户名与外部身份标识建立绑定关系。
 * </p>
 *
 * @author abstergo
 */
@Data
@Schema(description = "外部身份手工绑定表单")
public class ExternalIdentityBindForm implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 外部身份类型（如 wecom）
     */
    @Schema(description = "外部身份类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String identityType;

    /**
     * 外部身份标识（如企微 userid）
     */
    @Schema(description = "外部身份标识", requiredMode = Schema.RequiredMode.REQUIRED)
    private String externalId;

    /**
     * 系统登录用户名
     */
    @Schema(description = "系统登录用户名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userName;
}
