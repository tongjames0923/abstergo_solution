package com.tbs.web.config;

import com.tbs.common.util.base.BeanUtil;
import com.tbs.web.config.property.WebAuthProperty;
import com.tbs.web.security.authentication.IAuthManager;
import com.tbs.web.security.authentication.center.IAuthenticationCenter;
import com.tbs.web.security.authentication.center.impl.NoImplAuthCenter;
import com.tbs.web.security.authentication.impl.SpringSecurityContextHolderAuthManager;
import com.tbs.web.security.impl.DefaultAuthAccessDeniedHandler;
import com.tbs.web.security.impl.DefaultAuthEntryPoint;
import com.tbs.web.security.impl.TokenAuthenticationProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
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
    @ConditionalOnMissingBean(AuthenticationEntryPoint.class)

    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new DefaultAuthEntryPoint();
    }

    @Bean
    @ConditionalOnMissingBean(AccessDeniedHandler.class)
    public AccessDeniedHandler accessDeniedHandler() {
        return new DefaultAuthAccessDeniedHandler();
    }

    @Bean
    @ConditionalOnMissingBean(AuthenticationProvider.class)
    AuthenticationProvider authenticationProvider() {
        return new TokenAuthenticationProvider();
    }

    @Bean
    @ConditionalOnMissingBean(IAuthenticationCenter.class)
    public IAuthenticationCenter authenticationCenter() {
        return BeanUtil.buildBeanFromProperties(() -> {
            return new NoImplAuthCenter();
        }, webAuthProperty.getAuthenticationCenterImpl(), BeanUtil::buildByEmptyArgs);
    }

    @Bean
    @ConditionalOnMissingBean(IAuthManager.class)
    public IAuthManager authManager() {
        return BeanUtil.buildBeanFromProperties(SpringSecurityContextHolderAuthManager::new,
            webAuthProperty.getAuthManagerImpl(), BeanUtil::buildByEmptyArgs);
    }
}
