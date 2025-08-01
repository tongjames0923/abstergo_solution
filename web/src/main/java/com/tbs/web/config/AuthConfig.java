package com.tbs.web.config;

import com.tbs.common.util.BeanUtil;
import com.tbs.web.config.property.WebAuthProperty;
import com.tbs.web.security.IUserAuthService;
import com.tbs.web.security.impl.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.annotation.Resource;

/**
 * 认证配置
 *
 * @author abstergo
 */
@Configuration
public class AuthConfig {

    @Resource
    WebAuthProperty webAuthProperty;

    @Bean
    @ConditionalOnProperty(prefix = "tbs.web.auth", name = "enable", havingValue = "true")
    @ConditionalOnMissingBean(AuthenticationEntryPoint.class)
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new DefaultAuthEntryPoint();
    }

    @Bean
    @ConditionalOnProperty(prefix = "tbs.web.auth", name = "enable", havingValue = "true")
    @ConditionalOnMissingBean(AccessDeniedHandler.class)
    public AccessDeniedHandler accessDeniedHandler() {
        return new DefaultAuthAccessDeniedHandler();
    }

    @Bean
    @ConditionalOnProperty(prefix = "tbs.web.auth", name = "enable", havingValue = "true")
    @ConditionalOnMissingBean(AuthenticationProvider.class)
    AuthenticationProvider authenticationProvider() {
        return new TokenAuthenticationProvider();
    }

    @Bean
    @ConditionalOnProperty(prefix = "tbs.web.auth", name = "enable", havingValue = "true")
    @ConditionalOnMissingBean(UserDetailsService.class)
    public UserDetailsService userDetailsService() {
        return new DefaultUserDetailsService();
    }

    @Bean
    @ConditionalOnProperty(prefix = "tbs.web.auth", name = "enable", havingValue = "true", matchIfMissing = true)
    public IUserAuthService defaultUserAuthService() {
        return BeanUtil.buildBeanFromProperties(() -> {
            return new NotImplUserAuthService();
        }, webAuthProperty.getUserAuthServiceImpl(), BeanUtil::buildByEmptyArgs);
    }
}
