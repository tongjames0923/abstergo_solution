package com.tbs.app.config;

import com.tbs.app.service.impl.user.DefaultUserInfoService;
import com.tbs.common.util.base.BeanUtil;
import com.tbs.shared.interfaces.constraint.IUserNamePasswordValidator;
import com.tbs.shared.interfaces.constraint.impl.SimpleUserValidator;
import com.tbs.app.intercept.SystemOperateInterceptor;
import com.tbs.web.security.token.DefaultTokenRemover;
import com.tbs.web.security.token.IRefreshTokenService;
import com.tbs.web.security.token.ITokenRemover;
import com.tbs.web.security.user.IUserInfoService;
import com.tbs.web.security.user.access.IRefreshAccessTokenProvider;
import com.tbs.web.security.user.access.IUsernamePasswordAccessTokenProvider;
import com.tbs.web.security.user.access.impl.DefaultRefreshAccessTokenProvider;
import com.tbs.web.security.user.access.impl.DefaultUserPasswordAccessTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author abstergo
 */
@Configuration
@Slf4j
public class DefaultAppConfig {
    @Resource
    private AppSettingProperty systemAppProperty;

    public DefaultAppConfig() {
        log.info("初始化内置默认系统配置...");
    }

    @Bean
    @ConditionalOnMissingBean(IUserNamePasswordValidator.class)
    @ConditionalOnProperty(prefix = "tbs.system", value = "enable", havingValue = "true", matchIfMissing = false)
    public IUserNamePasswordValidator userNamePasswordValidator() {
        return BeanUtil.buildBeanFromProperties(() -> new SimpleUserValidator(),
            systemAppProperty.getSystemUserNamePasswordValidatorImpl(), BeanUtil::buildByEmptyArgs);
    }

    @Bean
    @ConditionalOnMissingBean(IUserInfoService.class)
    @ConditionalOnProperty(prefix = "tbs.system", value = "enable", havingValue = "true", matchIfMissing = false)
    public IUserInfoService userInfoService() {
        return new DefaultUserInfoService();
    }

    @Bean
    @ConditionalOnMissingBean(IUsernamePasswordAccessTokenProvider.class)
    @ConditionalOnProperty(prefix = "tbs.system", value = "enable", havingValue = "true", matchIfMissing = false)
    IUsernamePasswordAccessTokenProvider usernamePasswordAccessTokenProvider() {
        return new DefaultUserPasswordAccessTokenProvider();
    }

    @Bean
    @ConditionalOnMissingBean(ITokenRemover.class)
    @ConditionalOnProperty(prefix = "tbs.system", value = "enable", havingValue = "true", matchIfMissing = false)
    public ITokenRemover tokenRemover() {
        return new DefaultTokenRemover();
    }

    @Bean
    @ConditionalOnBean(IRefreshTokenService.class)
    @ConditionalOnProperty(prefix = "tbs.system", value = "enable", havingValue = "true", matchIfMissing = false)
    @ConditionalOnMissingBean(IRefreshAccessTokenProvider.class)
    public IRefreshAccessTokenProvider refreshAccessTokenProvider() {
        return new DefaultRefreshAccessTokenProvider();
    }

    @Bean
    @ConditionalOnProperty(prefix = "tbs.system", name = {"enable-operate-log", "enable"}, havingValue = "true",
        matchIfMissing = true)
    SystemOperateInterceptor systemOperAop() {
        return new SystemOperateInterceptor();
    }
}
