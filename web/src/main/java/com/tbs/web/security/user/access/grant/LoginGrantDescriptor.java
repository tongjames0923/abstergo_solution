package com.tbs.web.security.user.access.grant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 登录授权方式元数据
 * <p>
 * 描述单一登录授权方式的对外可见信息，供「启用登录方式下发」接口导出，
 * 支撑前端登录方式清单由静态配置向后端下发演进。
 * </p>
 *
 * @author abstergo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginGrantDescriptor {

    /**
     * 授权类型唯一标识（与前端登录方式 Provider id 对齐）
     */
    private String grantType;

    /**
     * 展示文案 i18n key（由前端翻译展示）
     */
    private String labelKey;

    /**
     * 可下发给前端的公开参数（如企微 corpId/agentId）
     */
    private Map<String, Object> publicOptions;
}
