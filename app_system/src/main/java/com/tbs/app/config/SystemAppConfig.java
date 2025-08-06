package com.tbs.app.config;

import com.tbs.app.constraint.IUserNamePasswordValidator;
import com.tbs.app.constraint.impl.SimpleUserValidator;
import com.tbs.app.service.business.IPasswordEncrypt;
import com.tbs.app.service.business.ISystemRoleService;
import com.tbs.app.service.business.ISystemUserService;
import com.tbs.app.service.business.IUserRoleRelationService;
import com.tbs.app.service.business.impl.NoEncryptPassword;
import com.tbs.app.service.business.impl.SimpleSystemRoleServiceImpl;
import com.tbs.app.service.business.impl.SimpleSystemUserServiceImpl;
import com.tbs.app.service.business.impl.SimpleUserRoleRelationServiceImpl;
import com.tbs.common.data.IDataHolderExpireActuator;
import com.tbs.common.data.impl.LocalDataExpireActuator;
import com.tbs.common.util.BeanUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import javax.annotation.Resource;

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
    @ConditionalOnMissingBean(IUserRoleRelationService.class)
    public IUserRoleRelationService userRoleRelationService() {
        return new SimpleUserRoleRelationServiceImpl();
    }
}
