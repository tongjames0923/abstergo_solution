package com.tbs.app.config;

import com.tbs.app.service.company.ISystemCompanyService;
import com.tbs.app.service.department.IDepartmentUserRelationService;
import com.tbs.app.service.department.ISystemDepartmentService;
import com.tbs.app.service.impl.company.CompanyTreeStructService;
import com.tbs.app.service.impl.company.SimpleCompanyService;
import com.tbs.app.service.impl.department.DepartmentTreeStructService;
import com.tbs.app.service.impl.department.SimpleDepartmentService;
import com.tbs.app.service.impl.department.SimpleDepartmentUserRelationService;
import com.tbs.app.service.impl.log.SimpleSystemLogServiceImpl;
import com.tbs.app.service.impl.role.SimpleSystemRoleServiceImpl;
import com.tbs.app.service.impl.role.SimpleUserRoleRelationServiceImpl;
import com.tbs.app.service.impl.token.DefaultSystemTokenService;
import com.tbs.app.service.impl.user.SimpleSystemUserServiceImpl;
import com.tbs.app.service.impl.view.ViewServiceImpl;
import com.tbs.app.service.log.ISystemLogService;
import com.tbs.app.service.role.ISystemRoleService;
import com.tbs.app.service.role.IUserRoleRelationService;
import com.tbs.app.service.token.ISystemTokenService;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.app.service.view.IViewService;
import com.tbs.app.web.DefaultUserLoginInfoService;
import com.tbs.common.error.IErrorThrowEvent;
import com.tbs.common.error.impl.SimpleErrorThrowEvent;
import com.tbs.common.util.base.BeanUtil;
import com.tbs.web.security.user.IPasswordEncrypt;
import com.tbs.web.security.user.IUserLoginInfoService;
import com.tbs.web.security.user.impl.NoEncryptPassword;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author abstergo
 */
@Configuration
public class BuisnessBeanConfig {

    @Resource
    private AppSettingProperty systemAppProperty;

    @Bean
    @ConditionalOnMissingBean(ISystemLogService.class)
    @ConditionalOnProperty(prefix = "tbs.system", value = "enable", havingValue = "true")
    public ISystemLogService systemLogService() {
        return new SimpleSystemLogServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(IErrorThrowEvent.class)
    @ConditionalOnProperty(prefix = "tbs.system", value = "enable", havingValue = "true")
    public IErrorThrowEvent errorThrowEvent() {
        return BeanUtil.buildBeanFromProperties(SimpleErrorThrowEvent::new, systemAppProperty.getErrorThrowEventImpl(),
            BeanUtil::buildByEmptyArgs);
    }

    @Bean
    @ConditionalOnMissingBean(IUserRoleRelationService.class)
    @ConditionalOnProperty(prefix = "tbs.system", value = "enable", havingValue = "true")
    public IUserRoleRelationService userRoleRelationService() {
        return new SimpleUserRoleRelationServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(ISystemUserService.class)
    @ConditionalOnProperty(prefix = "tbs.system", value = "enable", havingValue = "true")
    public ISystemUserService systemUserService() {
        return new SimpleSystemUserServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(ISystemCompanyService.class)
    @ConditionalOnProperty(prefix = "tbs.system", value = "enable", havingValue = "true")
    public ISystemCompanyService companyService() {
        return new SimpleCompanyService();
    }

    @Bean
    @ConditionalOnMissingBean(IViewService.class)
    @ConditionalOnProperty(prefix = "tbs.system", value = "enable", havingValue = "true")
    public IViewService viewService() {
        return new ViewServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(ISystemRoleService.class)
    @ConditionalOnProperty(prefix = "tbs.system", value = "enable", havingValue = "true")
    public ISystemRoleService roleService() {
        return new SimpleSystemRoleServiceImpl();
    }

    @Bean
    @ConditionalOnProperty(prefix = "tbs.system", value = "enable", havingValue = "true")
    public IPasswordEncrypt passwordEncrypt() {
        return BeanUtil.buildBeanFromProperties(NoEncryptPassword::new, systemAppProperty.getPasswordEncryptImpl(),
            BeanUtil::buildByEmptyArgs);
    }

    @Bean
    public CompanyTreeStructService companyTreeStructService() {
        return new CompanyTreeStructService();
    }

    @Bean
    public DepartmentTreeStructService departmentTreeStructService() {
        return new DepartmentTreeStructService();
    }


    @Bean
    @ConditionalOnMissingBean(ISystemDepartmentService.class)
    @ConditionalOnProperty(prefix = "tbs.system", value = "enable", havingValue = "true")
    public ISystemDepartmentService departmentService() {
        return new SimpleDepartmentService();
    }

    @Bean
    @ConditionalOnMissingBean(IDepartmentUserRelationService.class)
    @ConditionalOnProperty(prefix = "tbs.system", value = "enable", havingValue = "true")
    public IDepartmentUserRelationService departmentUserRelationService() {
        return new SimpleDepartmentUserRelationService();
    }

    @Bean
    @ConditionalOnMissingBean(IUserLoginInfoService.class)
    @ConditionalOnProperty(prefix = "tbs.system", value = "enable", havingValue = "true")
    public IUserLoginInfoService userLoginInfoService() {
        return new DefaultUserLoginInfoService();
    }

    @Bean
    @ConditionalOnMissingBean(ISystemTokenService.class)
    @ConditionalOnProperty(prefix = "tbs.system", value = "enable", havingValue = "true")
    public ISystemTokenService systemTokenService() {
        return new DefaultSystemTokenService();
    }

}
