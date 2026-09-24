package com.tbs.app.config;

import com.tbs.app.intercept.DataPermissionIntercepter;
import com.tbs.app.intercept.OperateLogAspect;
import com.tbs.app.intercept.SystemOperateInterceptor;
import com.tbs.app.security.grant.DefaultThirdPartyGrantProvider;
import com.tbs.app.security.grant.PasswordGrantProviderAdapter;
import com.tbs.app.security.grant.RefreshGrantProviderAdapter;
import com.tbs.app.service.IFeatureFlagService;
import com.tbs.app.service.identity.IExternalIdentityResolver;
import com.tbs.app.service.identity.IThirdPartyAppService;
import com.tbs.app.service.identity.IUserExternalIdentityService;
import com.tbs.app.service.impl.FeatureFlagServiceImpl;
import com.tbs.app.service.impl.identity.DefaultThirdPartyServiceImpl;
import com.tbs.app.service.impl.identity.SimpleUserExternalIdentityServiceImpl;
import com.tbs.app.service.impl.identity.UniqueKeyMatchResolver;
import com.tbs.app.service.impl.user.DefaultUserInfoService;
import com.tbs.common.util.base.BeanUtil;
import com.tbs.logging.core.*;
import com.tbs.shared.interfaces.constraint.IUserNamePasswordValidator;
import com.tbs.shared.interfaces.constraint.PasswordValidator;
import com.tbs.shared.interfaces.constraint.UserNameValidator;
import com.tbs.shared.interfaces.constraint.impl.SimpleUserValidator;
import com.tbs.shared.util.SqlLogicDeleteIntercepter;
import com.tbs.web.security.token.DefaultTokenRemover;
import com.tbs.web.security.token.IRefreshTokenService;
import com.tbs.web.security.token.ITokenRemover;
import com.tbs.web.security.user.IUserInfoService;
import com.tbs.web.security.user.access.IRefreshAccessTokenProvider;
import com.tbs.web.security.user.access.IUsernamePasswordAccessTokenProvider;
import com.tbs.web.security.user.access.impl.DefaultRefreshAccessTokenProvider;
import com.tbs.web.security.user.access.impl.DefaultUserPasswordAccessTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author abstergo
 */
@Configuration
@Slf4j
public class DefaultAppConfig {
    @Resource
    private AppSettingProperty systemAppProperty;

    public DefaultAppConfig() {
        log.info("初始化内置默认系统配置...");
    }



    @Bean
    @ConditionalOnMissingBean(IUserNamePasswordValidator.class)
    @ConditionalOnProperty(prefix = "tbs.system", value = "enable", havingValue = "true", matchIfMissing = false)
    public IUserNamePasswordValidator userNamePasswordValidator() {
        return BeanUtil.buildBeanFromProperties(() -> new SimpleUserValidator(),
            systemAppProperty.getSystemUserNamePasswordValidatorImpl(), BeanUtil::buildByEmptyArgs);
    }

    @Bean
    @ConditionalOnMissingBean(IFeatureFlagService.class)
    @ConditionalOnProperty(prefix = "tbs.system", value = "enable", havingValue = "true", matchIfMissing = false)
    public IFeatureFlagService featureFlagService() {
        return BeanUtil.buildBeanFromProperties(() -> new FeatureFlagServiceImpl(),
            systemAppProperty.getFeatureFlagServiceImpl(), BeanUtil::buildByEmptyArgs);

    }

    @Bean
    @ConditionalOnBean(IUserNamePasswordValidator.class)
    UserNameValidator userNameValidator() {
        return new UserNameValidator();
    }

    @Bean
    @ConditionalOnBean(IUserNamePasswordValidator.class)
    PasswordValidator passwordValidator() {
        return new PasswordValidator();
    }

    @Bean
    @ConditionalOnMissingBean(IUserInfoService.class)
    @ConditionalOnProperty(prefix = "tbs.system", value = "enable", havingValue = "true", matchIfMissing = false)
    public IUserInfoService userInfoService() {
        return new DefaultUserInfoService();
    }

    @Bean
    @ConditionalOnMissingBean(IUsernamePasswordAccessTokenProvider.class)
    @ConditionalOnProperty(prefix = "tbs.system", value = "enable", havingValue = "true", matchIfMissing = false)
    IUsernamePasswordAccessTokenProvider usernamePasswordAccessTokenProvider() {
        return new DefaultUserPasswordAccessTokenProvider();
    }

    /**
     * 账号密码登录授权适配器配置
     * <p>
     * 将既有账密令牌提供者桥接为 {@code password} 授权类型接入统一登录门面， 既有实现零修改；适配器随账密提供者存在而装配。
     * </p>
     *
     * @param delegate 既有账号密码访问令牌提供者
     * @return 账号密码登录授权适配器
     */
    @Bean
    @ConditionalOnBean(IUsernamePasswordAccessTokenProvider.class)
    @ConditionalOnMissingBean(PasswordGrantProviderAdapter.class)
    PasswordGrantProviderAdapter passwordGrantProvider(IUsernamePasswordAccessTokenProvider delegate) {
        return new PasswordGrantProviderAdapter(delegate);
    }



    /**
     * 刷新令牌授权适配器配置
     * <p>
     * 将刷新令牌换发访问令牌的能力桥接为 {@code refresh} 授权类型接入统一登录门面，
     * 复用既有 {@link com.tbs.web.security.token.IRefreshTokenService#useRefreshToken()} 全链路（刷新令牌解析、校验链、
     * 换发访问令牌、一次性失效），使「刷新」与「登录」收敛到同一端点 {@code POST /public/user/login/refresh}。
     * 适配器以 {@code ObjectProvider} 注入 {@link com.tbs.web.security.token.IRefreshTokenService}，
     * 避免刷新令牌服务缺失时启动失败；当容器中不存在该服务 Bean 时，刷新授权类型自动不启用。
     * </p>
     *
     * @param objectProvider 刷新令牌服务 Bean 的延迟/可选提供者
     * @return 刷新令牌登录授权适配器
     */
    @Bean
    @ConditionalOnMissingBean(RefreshGrantProviderAdapter.class)
    RefreshGrantProviderAdapter refreshGrantProviderAdapter(ObjectProvider<IRefreshTokenService> objectProvider) {
        return new RefreshGrantProviderAdapter(objectProvider);
    }

    /**
     * 第三方应用管理服务配置
     * <p>
     * 无特殊需求时无需配置，仅用于收集容器中所有第三方应用注册信息，提供给第三方应用管理接口查询。
     * </p>
     *
     * @return 第三方应用管理服务
     */
    @Bean
    @ConditionalOnMissingBean(IThirdPartyAppService.class)
    IThirdPartyAppService thirdPartyAppService() {
        return new DefaultThirdPartyServiceImpl();
    }

    /**
     * 第三方授权适配器配置
     * <p>
     * 无特殊需求时无需配置，仅用于收集容器中所有第三方授权注册信息，提供给第三方授权管理接口查询。
     * </p>
     *
     * @return
     */
    @Bean
    DefaultThirdPartyGrantProvider defaultThirdPartyGrantProvider() {
        return new DefaultThirdPartyGrantProvider();
    }


    /**
     * 约定式唯一标识匹配策略配置（默认自动关联策略）
     * <p>
     * 外部身份标识与系统登录用户名一致时自动关联；作为兜底策略仅在没有其他
     * {@code IExternalIdentityResolver} 实现时装配，不阻挡业务方注册更高优先级策略。
     * </p>
     *
     * @return 约定式唯一标识匹配策略
     */
    @Bean
    @ConditionalOnMissingBean(IExternalIdentityResolver.class)
    @ConditionalOnProperty(prefix = "tbs.system", value = "enable", havingValue = "true", matchIfMissing = false)
    public IExternalIdentityResolver uniqueKeyMatchResolver() {
        return new UniqueKeyMatchResolver();
    }

    /**
     * 外部身份绑定管理服务配置
     * <p>
     * 收集容器中全部自动关联策略（按 Ordered 排序）装配绑定管理服务，
     * 供外部登录链路与绑定管理接口共用。
     * </p>
     *
     * @param resolvers 容器中全部自动关联策略
     * @return 外部身份绑定管理服务
     */
    @Bean
    @ConditionalOnMissingBean(IUserExternalIdentityService.class)
    @ConditionalOnProperty(prefix = "tbs.system", value = "enable", havingValue = "true", matchIfMissing = false)
    public IUserExternalIdentityService userExternalIdentityService(List<IExternalIdentityResolver> resolvers) {
        return new SimpleUserExternalIdentityServiceImpl(resolvers);
    }

    @Bean
    @ConditionalOnMissingBean(ITokenRemover.class)
    @ConditionalOnProperty(prefix = "tbs.system", value = "enable", havingValue = "true", matchIfMissing = false)
    public ITokenRemover tokenRemover() {
        return new DefaultTokenRemover();
    }

    @Bean
    @ConditionalOnBean(IRefreshTokenService.class)
    @ConditionalOnProperty(prefix = "tbs.system", value = "enable", havingValue = "true", matchIfMissing = false)
    @ConditionalOnMissingBean(IRefreshAccessTokenProvider.class)
    public IRefreshAccessTokenProvider refreshAccessTokenProvider() {
        return new DefaultRefreshAccessTokenProvider();
    }

    @Bean
    @ConditionalOnProperty(prefix = "tbs.system", name = {"enable-operate-log", "enable"}, havingValue = "true",
        matchIfMissing = true)
    SystemOperateInterceptor systemOperAop() {
        return new SystemOperateInterceptor();
    }

    /**
     * 日志上下文解析门面配置
     * <p>
     * 收集全部来源模块注册的上下文创建器（如 web 模块的
     * WebLoggingContextCreator）与上下文拦截器，装配统一解析门面，
     * 供接口级与方法级日志采集链路获取环境维度信息。
     * </p>
     *
     * @param creators     上下文创建器集合，可为空
     * @param interceptors 上下文拦截器集合，可为空
     * @return 日志上下文解析门面
     */
    @Bean
    @ConditionalOnMissingBean(ILoggingContextResolver.class)
    public ILoggingContextResolver loggingContextResolver(List<ILoggingContextCreator> creators,
        List<ILoggingContextInterceptor> interceptors) {
        return new DefaultLoggingContextResolver(creators, interceptors);
    }

    /**
     * 日志上下文回填器配置
     *
     * @return 默认日志上下文回填器
     */
    @Bean
    @ConditionalOnMissingBean(ILoggingContextApplier.class)
    public ILoggingContextApplier loggingContextApplier() {
        return new DefaultLoggingContextApplier();
    }

    /**
     * 方法级操作日志切面配置
     * <p>
     * 仅拦截标注 {@code @OperateLog} 的方法，需显式开启
     * {@code tbs.system.enable-method-log=true}（默认关闭）。
     * </p>
     *
     * @return 方法级操作日志切面
     */
    @Bean
    @ConditionalOnProperty(prefix = "tbs.system", name = "enable-method-log", havingValue = "true",
        matchIfMissing = false)
    public OperateLogAspect operateLogAspect() {
        return new OperateLogAspect();
    }

    /**
     * SQL逻辑删除拦截器配置
     * <p>
     * 配置SQL逻辑删除功能，用于处理数据库记录的软删除
     * </p>
     *
     * @return SQL逻辑删除拦截器实例
     */
    @Bean
    public SqlLogicDeleteIntercepter sqlLogicDeleteIntercepter() {
        return new SqlLogicDeleteIntercepter();
    }

    /**
     * 数据权限拦截器配置
     * <p>
     * 配置数据权限功能，用于处理数据库记录的权限控制
     * </p>
     *
     * @return 数据权限拦截器实例
     */
    @Bean
    public DataPermissionIntercepter dataPermissionIntercepter() {
        return new DataPermissionIntercepter();
    }

}
