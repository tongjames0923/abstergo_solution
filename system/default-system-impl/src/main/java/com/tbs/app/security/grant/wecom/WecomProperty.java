package com.tbs.app.security.grant.wecom;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 企业微信登录配置属性
 * <p>
 * 收拢企业微信登录相关配置；{@code enable} 为模块总开关，
 * corpId/agentId 为公开参数可下发前端，corpSecret 为敏感参数仅后端持有。
 * </p>
 *
 * @author abstergo
 */
@Data
@ConfigurationProperties(prefix = "tbs.wecom")
public class WecomProperty {

    /**
     * 企业微信登录总开关
     */
    private boolean enable = false;

    /**
     * 企业微信企业 ID（公开参数，可下发前端）
     */
    private String corpId;

    /**
     * 企业微信应用 Secret（敏感参数，仅后端持有，禁止下发前端）
     */
    private String corpSecret;

    /**
     * 企业微信应用 AgentId（公开参数，可下发前端）
     */
    private String agentId;
}
