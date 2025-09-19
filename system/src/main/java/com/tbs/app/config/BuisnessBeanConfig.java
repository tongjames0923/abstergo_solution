package com.tbs.app.config;

import com.tbs.app.service.business.company.ICompanyUserRelationService;
import com.tbs.app.service.business.company.ISystemCompanyService;
import com.tbs.app.service.business.company.impl.SimpleCompanyService;
import com.tbs.app.service.business.company.impl.SimpleCompanyUserRelationService;
import com.tbs.app.service.business.role.ISystemRoleService;
import com.tbs.app.service.business.role.IUserRoleRelationService;
import com.tbs.app.service.business.role.impl.SimpleSystemRoleServiceImpl;
import com.tbs.app.service.business.role.impl.SimpleUserRoleRelationServiceImpl;
import com.tbs.app.service.business.token.ISystemTokenService;
import com.tbs.app.service.business.token.impl.DefaultSystemTokenService;
import com.tbs.app.service.business.user.ISystemUserService;
import com.tbs.app.service.business.user.impl.SimpleSystemUserServiceImpl;
import com.tbs.app.service.business.view.IViewService;
import com.tbs.app.service.business.view.impl.ViewServiceImpl;
import com.tbs.app.web.login.DefaultUserLoginInfoService;
import com.tbs.common.util.BeanUtil;
import com.tbs.web.security.user.IPasswordEncrypt;
import com.tbs.web.security.user.IUserLoginInfoService;
import com.tbs.web.security.user.impl.NoEncryptPassword;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
@ConditionalOnProperty(prefix = "tbs.system", value = "enable", havingValue = "true", matchIfMissing = false)
public class BuisnessBeanConfig {

    @Resource
    private AppSettingProperty systemAppProperty;

    @Bean
    @ConditionalOnMissingBean(IUserRoleRelationService.class)
    public IUserRoleRelationService userRoleRelationService() {
        return new SimpleUserRoleRelationServiceImpl();
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
    @ConditionalOnMissingBean(ICompanyUserRelationService.class)
    public ICompanyUserRelationService companyUserRelationService() {
        return new SimpleCompanyUserRelationService();
    }

    @Bean
    @ConditionalOnMissingBean(IUserLoginInfoService.class)
    public IUserLoginInfoService userLoginInfoService() {
        return new DefaultUserLoginInfoService();
    }

    @Bean
    @ConditionalOnMissingBean(ISystemTokenService.class)
    public ISystemTokenService systemTokenService() {
        return new DefaultSystemTokenService();
    }

}
