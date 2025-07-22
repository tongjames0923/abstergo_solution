package com.tbs.web.config;

import com.tbs.common.util.BeanUtil;
import com.tbs.web.config.property.WebAuthProperty;
import com.tbs.web.security.IUserAuthService;
import com.tbs.web.security.impl.AuthExceptionHandler;
import com.tbs.web.security.impl.UserAuthServiceImpl;
import com.tbs.web.security.impl.NotImplUserAuthService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;

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
    public AuthExceptionHandler authExceptionHandler() {
        return new AuthExceptionHandler();
    }

    @Bean
    @ConditionalOnProperty(prefix = "tbs.web.auth", name = "enable", havingValue = "true")
    AuthenticationProvider authenticationProvider() {
        return new UserAuthServiceImpl();
    }

    @Bean
    @ConditionalOnProperty(prefix = "tbs.web.auth", name = "enable", havingValue = "true", matchIfMissing = true)
    public IUserAuthService notImplUserAuthService() {
        return BeanUtil.buildBeanFromProperties(() -> {
            return new NotImplUserAuthService();
        }, webAuthProperty.getUserAuthServiceImpl(), BeanUtil::buildByEmptyArgs);
    }
}
