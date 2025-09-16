package com.tbs.app.config;

import com.tbs.app.service.business.user.impl.DefaultUserAuthService;
import com.tbs.app.service.business.user.impl.DefaultUserInfoService;
import com.tbs.common.data.IDataHolderExpireActuator;
import com.tbs.common.data.impl.LocalDataExpireActuator;
import com.tbs.common.util.BeanUtil;
import com.tbs.shared.interfaces.constraint.IUserNamePasswordValidator;
import com.tbs.shared.interfaces.constraint.impl.SimpleUserValidator;
import com.tbs.shared.interfaces.intercept.SystemOperateInterceptor;
import com.tbs.web.security.token.IRefreshTokenService;
import com.tbs.web.security.token.impl.CookieRefreshTokenService;
import com.tbs.web.security.user.IUserAuthService;
import com.tbs.web.security.user.IUserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author abstergo
 */
@Configuration
@ConditionalOnProperty(prefix = "tbs.system", value = "enable", havingValue = "true", matchIfMissing = false)
@Slf4j
public class DefaultAppConfig {
    @Resource
    private AppSettingProperty systemAppProperty;

    public DefaultAppConfig() {
        log.info("初始化内置默认系统配置...");
    }

    @Bean
    @ConditionalOnMissingBean(IUserNamePasswordValidator.class)
    public IUserNamePasswordValidator userNamePasswordValidator() {
        return BeanUtil.buildBeanFromProperties(() -> new SimpleUserValidator(),
            systemAppProperty.getSystemUserNamePasswordValidatorImpl(), BeanUtil::buildByEmptyArgs);
    }

    @Bean
    IDataHolderExpireActuator expireActuator() {
        return BeanUtil.buildBeanFromProperties(LocalDataExpireActuator::new,
            systemAppProperty.getExpireActuatorClass(), BeanUtil::buildByEmptyArgs);
    }

    @Bean
    @ConditionalOnMissingBean(IUserInfoService.class)
    public IUserInfoService userInfoService() {
        return new DefaultUserInfoService();
    }

    @Bean
    @ConditionalOnMissingBean(IRefreshTokenService.class)
    public IRefreshTokenService refreshTokenService() {
        return new CookieRefreshTokenService();
    }

    @Bean
    @ConditionalOnMissingBean(IUserAuthService.class)
    public IUserAuthService userAuthService() {
        return new DefaultUserAuthService();
    }

    @Bean
    @ConditionalOnProperty(prefix = "tbs.system", name = "enable-operate-log", havingValue = "true",
        matchIfMissing = true)
    SystemOperateInterceptor systemOperAop() {
        return new SystemOperateInterceptor();
    }
}
