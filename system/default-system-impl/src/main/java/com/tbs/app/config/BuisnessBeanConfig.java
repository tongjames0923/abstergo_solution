package com.tbs.app.config;

import com.tbs.app.service.company.ISystemCompanyService;
import com.tbs.app.service.department.IDepartmentUserRelationService;
import com.tbs.app.service.department.ISystemDepartmentService;
import com.tbs.app.service.genericapi.IGenericApiService;
import com.tbs.app.service.impl.company.CompanyTreeStructService;
import com.tbs.app.service.impl.company.SimpleCompanyService;
import com.tbs.app.service.impl.department.DepartmentTreeStructService;
import com.tbs.app.service.impl.department.SimpleDepartmentService;
import com.tbs.app.service.impl.department.SimpleDepartmentUserRelationService;
import com.tbs.app.service.impl.genericapi.GenericApiServiceImpl;
import com.tbs.app.service.impl.push.GrantedTopicSubscriptionAuthorizer;
import com.tbs.app.service.impl.push.PushSessionIdentityInterceptor;
import com.tbs.app.service.impl.push.PushSessionUploadGuard;
import com.tbs.app.service.impl.push.PushUserAuthenticator;
import com.tbs.app.service.impl.push.SignalChannelTopicGranter;
import com.tbs.app.service.impl.push.TopicPrefixNotifyReceiverResolver;
import com.tbs.app.service.impl.push.PushNotifyServiceImpl;
import com.tbs.app.service.impl.role.SimpleRoleGroupServiceImpl;
import com.tbs.app.service.impl.role.SimpleSystemRoleServiceImpl;
import com.tbs.app.service.impl.token.DefaultSystemTokenService;
import com.tbs.app.service.impl.user.SimpleSystemUserServiceImpl;
import com.tbs.app.service.impl.version.AppPackageServiceImpl;
import com.tbs.app.service.impl.version.AppVersionCompatibilityPolicyServiceImpl;
import com.tbs.app.service.impl.version.AppVersionServiceImpl;
import com.tbs.app.service.impl.view.ViewServiceImpl;
import com.tbs.app.service.push.INotifyReceiverResolver;
import com.tbs.app.service.push.IPushNotifyService;
import com.tbs.app.service.role.ISystemRoleGroupService;
import com.tbs.app.service.role.ISystemRoleService;
import com.tbs.app.service.token.ISystemTokenService;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.app.service.version.IAppPackageService;
import com.tbs.app.service.version.IAppVersionCompatibilityPolicyService;
import com.tbs.app.service.version.IAppVersionService;
import com.tbs.app.service.view.IViewService;
import com.tbs.app.web.DefaultUserLoginInfoService;
import com.tbs.common.error.IErrorThrowEvent;
import com.tbs.common.error.impl.SimpleErrorThrowEvent;
import com.tbs.common.util.base.BeanUtil;
import com.tbs.push.api.auth.IPushAuthenticator;
import com.tbs.push.api.auth.IPushTopicGranter;
import com.tbs.push.api.intercept.IConnectionInterceptor;
import com.tbs.push.api.session.ISubscriptionAuthorizer;
import com.tbs.push.api.session.IUploadGuard;
import com.tbs.web.security.user.IPasswordEncrypt;
import com.tbs.web.security.user.IUserLoginInfoService;
import com.tbs.web.security.user.impl.NoEncryptPassword;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.annotation.Resource;

/**
 * @author abstergo
 */
@Configuration
public class BuisnessBeanConfig {

    @Resource
    private AppSettingProperty systemAppProperty;


    @Bean
    @ConditionalOnMissingBean(IErrorThrowEvent.class)
    @ConditionalOnProperty(prefix = "tbs.system", value = "enable", havingValue = "true")
    public IErrorThrowEvent errorThrowEvent() {
        return BeanUtil.buildBeanFromProperties(SimpleErrorThrowEvent::new, systemAppProperty.getErrorThrowEventImpl(),
            BeanUtil::buildByEmptyArgs);
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

    @Bean
    @ConditionalOnMissingBean(IGenericApiService.class)
    @ConditionalOnProperty(prefix = "tbs.system", value = "enable", havingValue = "true")
    public IGenericApiService genericApiService() {
        return new GenericApiServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(ISystemRoleGroupService.class)
    @ConditionalOnProperty(prefix = "tbs.system", value = "enable", havingValue = "true")
    public ISystemRoleGroupService systemRoleGroupService() {
        return new SimpleRoleGroupServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(IAppVersionService.class)
    @ConditionalOnProperty(prefix = "tbs.system", value = "enable", havingValue = "true")
    public IAppVersionService appVersionService() {
        return new AppVersionServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(IAppPackageService.class)
    @ConditionalOnProperty(prefix = "tbs.system", value = "enable", havingValue = "true")
    public IAppPackageService appPackageService() {
        return new AppPackageServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(IAppVersionCompatibilityPolicyService.class)
    @ConditionalOnProperty(prefix = "tbs.system", value = "enable", havingValue = "true")
    public IAppVersionCompatibilityPolicyService appVersionCompatibilityPolicyService() {
        return new AppVersionCompatibilityPolicyServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(IPushNotifyService.class)
    @ConditionalOnProperty(prefix = "tbs.system", value = "enable", havingValue = "true")
    public IPushNotifyService pushNotifyService() {
        return new PushNotifyServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(INotifyReceiverResolver.class)
    @ConditionalOnProperty(prefix = "tbs.system", value = "enable", havingValue = "true")
    public INotifyReceiverResolver notifyReceiverResolver() {
        return new TopicPrefixNotifyReceiverResolver();
    }

    /**
     * 业务连接认证器（签票时点身份绑定）
     * <p>
     * 不用@ConditionalOnMissingBean而用@Primary：与协议层Noop兜底的装配顺序
     * 无关，无论两个Bean是否同时注册，按类型注入均选中本实现。
     * 依赖登录用户解析（tbs.system.enable），推送开启而系统模块未启用时应
     * 自行注册业务IPushAuthenticator覆盖。
     * </p>
     *
     * @return 业务连接认证器
     */
    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "tbs.push", name = "enable", havingValue = "true")
    public IPushAuthenticator pushUserAuthenticator() {
        return new PushUserAuthenticator();
    }

    /**
     * 内置主题授权器（本人信令频道 + 配置公开白名单）
     *
     * @return 主题授权器
     */
    @Bean
    @ConditionalOnProperty(prefix = "tbs.push", name = "enable", havingValue = "true")
    public IPushTopicGranter signalChannelTopicGranter() {
        return new SignalChannelTopicGranter();
    }

    /**
     * 授权集订阅校验器（订阅时点鉴权，保留前缀拒订）
     *
     * @return 订阅授权校验器
     */
    @Bean
    @ConditionalOnProperty(prefix = "tbs.push", name = "enable", havingValue = "true")
    public ISubscriptionAuthorizer subscriptionAuthorizer() {
        return new GrantedTopicSubscriptionAuthorizer();
    }

    /**
     * 会话身份守卫拦截器（建连一致性校验 + 本人频道自动订阅）
     *
     * @return 连接拦截器
     */
    @Bean
    @ConditionalOnProperty(prefix = "tbs.push", name = "enable", havingValue = "true")
    public IConnectionInterceptor pushSessionIdentityInterceptor() {
        return new PushSessionIdentityInterceptor();
    }

    /**
     * 会话归属上行守卫（协议层守卫链，SSE/WebSocket 上行统一认人）
     *
     * @return 上行守卫
     */
    @Bean
    @ConditionalOnProperty(prefix = "tbs.push", name = "enable", havingValue = "true")
    public IUploadGuard pushSessionUploadGuard() {
        return new PushSessionUploadGuard();
    }

}
