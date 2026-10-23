package com.tbs.app.service.identity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 外部身份类型元数据描述符
 * <p>
 * 供前端渲染「可绑定平台清单」（如管理端手工绑定的身份类型下拉框），
 * 由后端运行时能力（已注册的 {@code IExternalIdentityExchanger} 与外部登录 Provider）
 * 聚合导出，前端无需再硬编码平台列表。
 * </p>
 *
 * @author abstergo
 * @see IExternalIdentityExchanger 平台凭证换取 SPI（决定 selfBindable）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalIdentityTypeDescriptor {

    /**
     * 外部身份类型标识（与绑定表 identity_type 一致，如 wecom / mes）
     */
    private String identityType;

    /**
     * 展示文案 i18n key（约定 {@code identity.type.{identityType}}，由前端翻译展示）
     */
    private String labelKey;

    /**
     * 是否支持登录态自助绑定（存在对应 {@link IExternalIdentityExchanger} 即为 {@code true}）
     */
    private boolean selfBindable;
}
