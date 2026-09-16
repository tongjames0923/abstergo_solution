package com.tbs.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tbs.web.security.user.access.grant.ILoginGrantProvider;
import com.tbs.web.security.user.access.grant.LoginGrantRegistry;
import com.tbs.web.security.user.access.grant.LoginGrantService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 登录授权通用能力配置
 * <p>
 * 装配登录授权注册表与统一门面；业务模块只需注册 {@link ILoginGrantProvider} 实现 Bean
 * 即被注册表自动收集，无需改动本配置。
 * </p>
 *
 * @author abstergo
 * @see ILoginGrantProvider 登录授权提供者 SPI
 */
@Configuration
public class LoginGrantConfig {

    /**
     * 登录授权注册表
     *
     * @param providers 容器中全部登录授权提供者，可为空
     * @return 注册表实例
     */
    @Bean
    @ConditionalOnMissingBean(LoginGrantRegistry.class)
    public LoginGrantRegistry loginGrantRegistry(List<ILoginGrantProvider<?>> providers) {
        return new LoginGrantRegistry(providers);
    }

    /**
     * 登录授权统一门面
     *
     * @param registry     登录授权注册表
     * @param objectMapper JSON 映射器（复用 Spring Boot 容器实例）
     * @return 门面实例
     */
    @Bean
    @ConditionalOnMissingBean(LoginGrantService.class)
    public LoginGrantService loginGrantService(LoginGrantRegistry registry, ObjectMapper objectMapper) {
        return new LoginGrantService(registry, objectMapper);
    }
}
