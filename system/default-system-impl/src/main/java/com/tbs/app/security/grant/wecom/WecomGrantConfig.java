package com.tbs.app.security.grant.wecom;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 企业微信登录授权配置
 * <p>
 * 按 {@code tbs.wecom.enable} 总开关装配企业微信 API 客户端与登录授权提供者；
 * 提供者注册为 Spring Bean 后由统一登录门面的注册表自动收集，无需改动控制器与门面。
 * </p>
 *
 * @author abstergo
 */
@Configuration
@ConditionalOnProperty(prefix = "tbs.wecom", name = "enable", havingValue = "true", matchIfMissing = false)
@EnableConfigurationProperties(WecomProperty.class)
public class WecomGrantConfig {

    /**
     * 企业微信 API 客户端
     *
     * @param property 企业微信登录配置属性
     * @return 客户端实例
     */
    @Bean
    @ConditionalOnMissingBean(WecomApiClient.class)
    public WecomApiClient wecomApiClient(WecomProperty property) {
        return new WecomApiClient(property);
    }

    /**
     * 企业微信凭证换取实现
     *
     * @param apiClient 企业微信 API 客户端
     * @return 凭证换取实现实例
     */
    @Bean
    @ConditionalOnMissingBean(WecomIdentityExchanger.class)
    public WecomIdentityExchanger wecomIdentityExchanger(WecomApiClient apiClient) {
        return new WecomIdentityExchanger(apiClient);
    }

    /**
     * 企业微信登录授权提供者
     *
     * @param property         企业微信登录配置属性
     * @param identityExchanger 企微凭证换取实现（与自助绑定链路共用）
     * @return 提供者实例
     */
    @Bean
    @ConditionalOnMissingBean(WecomLoginGrantProvider.class)
    public WecomLoginGrantProvider wecomLoginGrantProvider(WecomProperty property,
        WecomIdentityExchanger identityExchanger) {
        return new WecomLoginGrantProvider(property, identityExchanger);
    }
}
