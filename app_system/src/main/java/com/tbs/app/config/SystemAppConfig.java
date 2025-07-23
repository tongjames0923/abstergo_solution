package com.tbs.app.config;

import com.tbs.app.constant.SystemAppConstant;
import com.tbs.app.constraint.IUserNamePasswordValidator;
import com.tbs.app.constraint.impl.SimpleUserValidator;
import com.tbs.app.service.business.IPasswordEncrypt;
import com.tbs.app.service.business.ISystemRoleService;
import com.tbs.app.service.business.ISystemUserService;
import com.tbs.app.service.business.impl.NoEncryptPassword;
import com.tbs.app.service.business.impl.SimpleSystemRoleServiceImpl;
import com.tbs.app.service.business.impl.SimpleSystemUserServiceImpl;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.holder.context.IContext;
import com.tbs.common.holder.context.impl.LocalStoredMapImplContext;
import com.tbs.common.util.BeanUtil;
import com.tbs.web.security.model.SimpleTokenModel;
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
    @ConditionalOnMissingBean(ISystemUserService.class)
    public ISystemUserService systemUserService() {
        return BeanUtil.buildBeanFromProperties(SimpleSystemUserServiceImpl::new,
            systemAppProperty.getSystemUserServiceImpl(), BeanUtil::buildByEmptyArgs);
    }

    @Bean
    IContext<SimpleTokenModel> tokenModelContext() {
        return BeanUtil.buildBeanFromProperties(() -> {
            return new LocalStoredMapImplContext<>(SystemAppConstant.TOKEN_CONTEXT_ID);
        }, systemAppProperty.getTokenContextImpl(), (c) -> {
            try {
                return c.getConstructor(String.class).newInstance(SystemAppConstant.TOKEN_CONTEXT_ID);
            } catch (Throwable e) {
                throw new FormattedRuntimeException("HolderProperty.tokenContextImpl  Error to build bean");
            }
        });
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
}
