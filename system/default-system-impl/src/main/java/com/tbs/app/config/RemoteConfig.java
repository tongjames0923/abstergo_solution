package com.tbs.app.config;

import com.tbs.app.remote.auth.IAuthRemoteService;
import com.tbs.app.remote.auth.impl.DefaultAuthRemoteServiceImpl;
import com.tbs.app.remote.user.IUserRemoteService;
import com.tbs.app.remote.user.impl.DefaultUserRemoteServiceImpl;
import com.tbs.common.util.base.BeanUtil;
import com.tbs.struct.tree.api.persistence.ITreeStore;
import com.tbs.struct.tree.api.persistence.impl.NoImplTreeStore;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

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
    @DubboService
    ITreeStore treeStore() {
        return BeanUtil.buildBeanFromProperties(NoImplTreeStore::new, property.getTreeStoreImpl(),
            BeanUtil::buildByEmptyArgs);
    }

    @Bean
    @DubboService
    IAuthRemoteService authRemoteService() {
        return BeanUtil.buildBeanFromProperties(DefaultAuthRemoteServiceImpl::new, property.getAuthRemoteServiceImpl(),
            BeanUtil::buildByEmptyArgs);
    }

    @Bean
    @DubboService
    IUserRemoteService userRemoteService() {
        return BeanUtil.buildBeanFromProperties(DefaultUserRemoteServiceImpl::new, property.getUserRemoteServiceImpl(),
            BeanUtil::buildByEmptyArgs);
    }
}
