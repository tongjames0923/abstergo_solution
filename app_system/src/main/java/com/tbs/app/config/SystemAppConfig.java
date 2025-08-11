package com.tbs.app.config;

import javax.annotation.Resource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import com.tbs.app.constraint.IUserNamePasswordValidator;
import com.tbs.app.constraint.impl.SimpleUserValidator;
import com.tbs.app.intercept.SystemOperateInterceptor;
import com.tbs.app.service.business.IPasswordEncrypt;
import com.tbs.app.service.business.ISystemRoleService;
import com.tbs.app.service.business.ISystemUserService;
import com.tbs.app.service.business.IUserRoleRelationService;
import com.tbs.app.service.business.impl.*;
import com.tbs.common.data.IDataHolderExpireActuator;
import com.tbs.common.data.impl.LocalDataExpireActuator;
import com.tbs.common.util.BeanUtil;
import com.tbs.web.security.ITokenManager;
import com.tbs.web.security.IUserInfoService;

/**
 * @author abstergo
 */
public class SystemAppConfig {
	@Resource
	private SystemAppProperty systemAppProperty;

	@Bean
	@ConditionalOnMissingBean(IUserNamePasswordValidator.class)
	public IUserNamePasswordValidator userNamePasswordValidator() {
		return BeanUtil.buildBeanFromProperties(SimpleUserValidator::new,
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
