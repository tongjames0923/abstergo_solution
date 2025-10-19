package com.tbs.app.config;

import com.tbs.app.service.company.ICompanyUserRelationService;
import com.tbs.app.service.company.ISystemCompanyService;
import com.tbs.app.service.impl.company.SimpleCompanyService;
import com.tbs.app.service.impl.company.SimpleCompanyUserRelationService;
import com.tbs.app.service.impl.role.SimpleSystemRoleServiceImpl;
import com.tbs.app.service.impl.role.SimpleUserRoleRelationServiceImpl;
import com.tbs.app.service.impl.token.DefaultSystemTokenService;
import com.tbs.app.service.impl.user.SimpleSystemUserServiceImpl;
import com.tbs.app.service.impl.view.ViewServiceImpl;
import com.tbs.app.service.role.ISystemRoleService;
import com.tbs.app.service.role.IUserRoleRelationService;
import com.tbs.app.service.token.ISystemTokenService;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.app.service.view.IViewService;
import com.tbs.app.web.DefaultUserLoginInfoService;
import com.tbs.common.util.base.BeanUtil;
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
        return new SimpleSystemUserServiceImpl();
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
