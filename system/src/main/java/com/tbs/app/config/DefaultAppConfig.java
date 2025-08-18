package com.tbs.app.config;

import javax.annotation.Resource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tbs.app.service.business.IPasswordEncrypt;
import com.tbs.app.service.business.ISystemRoleService;
import com.tbs.app.service.business.ISystemUserService;
import com.tbs.app.service.business.IUserRoleRelationService;
import com.tbs.app.service.business.impl.*;
import com.tbs.common.data.IDataHolderExpireActuator;
import com.tbs.common.data.impl.LocalDataExpireActuator;
import com.tbs.common.util.BeanUtil;
import com.tbs.shared.interfaces.constraint.IUserNamePasswordValidator;
import com.tbs.shared.interfaces.constraint.impl.SimpleUserValidator;
import com.tbs.shared.interfaces.intercept.SystemOperateInterceptor;
import com.tbs.web.security.ITokenManager;
import com.tbs.web.security.IUserInfoService;

import lombok.extern.slf4j.Slf4j;

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
	@ConditionalOnMissingBean(ISystemUserService.class)
	public ISystemUserService systemUserService() {
		return BeanUtil.buildBeanFromProperties(SimpleSystemUserServiceImpl::new,
				systemAppProperty.getSystemUserServiceImpl(), BeanUtil::buildByEmptyArgs);
	}

	@Bean
	@ConditionalOnMissingBean(ISystemRoleService.class)
	public ISystemRoleService roleService() {
		return new SimpleSystemRoleServiceImpl();
	}

	@Bean
	public IPasswordEncrypt passwordEncrypt() {
		return BeanUtil.buildBeanFromProperties(NoEncryptPassword::new, systemAppProperty.getPasswordEncryptImpl(),
				BeanUtil::buildByEmptyArgs);
	}

	@Bean
	@ConditionalOnMissingBean(IUserInfoService.class)
	public IUserInfoService userInfoService() {
		return new DefaultUserInfoService();
	}

	@Bean
	@ConditionalOnMissingBean(ITokenManager.class)
	public ITokenManager tokenManager() {
		return new DefaultTokenManager();
	}

	@Bean
	@ConditionalOnMissingBean(IUserRoleRelationService.class)
	public IUserRoleRelationService userRoleRelationService() {
		return new SimpleUserRoleRelationServiceImpl();
	}

	@Bean
	@ConditionalOnProperty(prefix = "tbs.system", name = "enable-operate-log", havingValue = "true", matchIfMissing = true)
	SystemOperateInterceptor systemOperAop() {
		return new SystemOperateInterceptor();
	}
}
