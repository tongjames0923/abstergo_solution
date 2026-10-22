package com.tbs.app.config;

import com.tbs.app.remote.auth.IAuthRemoteService;
import com.tbs.app.remote.auth.impl.DefaultAuthRemoteServiceImpl;
import com.tbs.app.remote.company.ICompanyRemoteService;
import com.tbs.app.remote.company.impl.DefaultCompanyRemoteServiceImpl;
import com.tbs.app.remote.user.IUserRemoteService;
import com.tbs.app.remote.user.impl.DefaultUserRemoteServiceImpl;
import com.tbs.app.service.impl.user.DefaultUserIdentityService;
import com.tbs.app.service.user.IUserIdentityService;
import com.tbs.common.constant.ProviderConstant;
import com.tbs.common.util.base.BeanUtil;
import com.tbs.struct.tree.api.persistence.ITreeStore;
import com.tbs.struct.tree.api.persistence.impl.NoImplTreeStore;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.Resource;

/**
 * <p>
 * 配置远程服务
 * </p>
 *
 * @author abstergo
 */
@Configuration
@ConditionalOnProperty(prefix = "tbs.system.remote", name = "enable", havingValue = "true")
public class RemoteConfig {

    @Resource
    private RemoteProperty property;

    /**
     * 闭包树服务配置
     * <p>
     * 配置闭包树服务，用于处理树形结构数据
     * </p>
     *
     * @return 闭包树服务实例
     */
    @Bean
    @DubboService(group = ProviderConstant.DEFAULT_BUSINESS)
    ITreeStore treeStore() {
        return BeanUtil.buildBeanFromProperties(NoImplTreeStore::new, property.getTreeStoreImpl(),
            BeanUtil::buildByEmptyArgs);
    }

    @Bean
    @DubboService(group = ProviderConstant.DEFAULT_BUSINESS)
    IAuthRemoteService authRemoteService() {
        return BeanUtil.buildBeanFromProperties(DefaultAuthRemoteServiceImpl::new, property.getAuthRemoteServiceImpl(),
            BeanUtil::buildByEmptyArgs);
    }

    @Bean
    @DubboService(group = ProviderConstant.DEFAULT_BUSINESS)
    IUserRemoteService userRemoteService() {
        return BeanUtil.buildBeanFromProperties(DefaultUserRemoteServiceImpl::new, property.getUserRemoteServiceImpl(),
            BeanUtil::buildByEmptyArgs);
    }

    @Bean
    @DubboService(group = ProviderConstant.DEFAULT_BUSINESS)
    ICompanyRemoteService companyRemoteService() {
        return BeanUtil.buildBeanFromProperties(DefaultCompanyRemoteServiceImpl::new, property.getCompanyRemoteServiceImpl(),
            BeanUtil::buildByEmptyArgs);
    }

    /**
     * 用户身份聚合服务
     * <p>
     * 对外暴露统一的身份装配入口：消费方一次调用即可拿到状态位、权限编码与部门归属，
     * 不必分别调用用户、角色、部门三个源服务，避免模块拆分后认证链路退化为多次串行远程调用。
     * </p>
     *
     * @return 身份聚合服务实例
     */
    @Bean
    @DubboService(group = ProviderConstant.DEFAULT_BUSINESS)
    IUserIdentityService userIdentityRemoteService() {
        return new DefaultUserIdentityService();
    }
}
