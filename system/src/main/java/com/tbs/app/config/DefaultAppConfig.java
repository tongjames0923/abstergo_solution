package com.tbs.app.config;

import javax.annotation.Resource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tbs.app.service.business.auth.impl.DefaultUserInfoService;
import com.tbs.app.service.business.company.ISystemCompanyService;
import com.tbs.app.service.business.company.impl.SimpleCompanyService;
import com.tbs.app.service.business.role.ISystemRoleService;
import com.tbs.app.service.business.role.IUserRoleRelationService;
import com.tbs.app.service.business.role.impl.SimpleSystemRoleServiceImpl;
import com.tbs.app.service.business.role.impl.SimpleUserRoleRelationServiceImpl;
import com.tbs.web.security.token.IRefreshTokenService;
import com.tbs.web.security.token.CookieRefreshTokenService;
import com.tbs.app.service.business.user.IPasswordEncrypt;
import com.tbs.app.service.business.user.ISystemUserService;
import com.tbs.app.service.business.user.impl.NoEncryptPassword;
import com.tbs.app.service.business.user.impl.SimpleSystemUserServiceImpl;
import com.tbs.app.service.business.view.IViewService;
import com.tbs.app.service.business.view.impl.ViewServiceImpl;
import com.tbs.common.data.IDataHolderExpireActuator;
import com.tbs.common.data.impl.LocalDataExpireActuator;
import com.tbs.common.util.BeanUtil;
import com.tbs.shared.interfaces.constraint.IUserNamePasswordValidator;
import com.tbs.shared.interfaces.constraint.impl.SimpleUserValidator;
import com.tbs.shared.interfaces.intercept.SystemOperateInterceptor;
import com.tbs.web.security.user.IUserInfoService;

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
	@ConditionalOnMissingBean(ISystemCompanyService.class)
	public ISystemCompanyService companyService() {
		return new SimpleCompanyService();
	}

	@Bean
	@ConditionalOnMissingBean(IViewService.class)
	public IViewService viewService() {
		return new ViewServiceImpl();
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
	@ConditionalOnMissingBean(IRefreshTokenService.class)
	public IRefreshTokenService refreshTokenService() {
		return new CookieRefreshTokenService();
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
