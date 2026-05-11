package com.tbs.app.config;

import com.tbs.app.remote.auth.IAuthRemoteService;
import com.tbs.app.remote.auth.impl.DefaultAuthRemoteServiceImpl;
import com.tbs.app.remote.user.IUserRemoteService;
import com.tbs.app.remote.user.impl.DefaultUserRemoteServiceImpl;
import com.tbs.struct.tree.api.persistence.ITreeStore;
import com.tbs.struct.tree.api.persistence.impl.NoImplTreeStore;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "tbs.system.remote")
@Data
@Component
public class RemoteProperty {
    /**
     * 是否启用远程服务
     */
    private boolean enable = false;
    /**
     * 树结构存储实现类
     */
    Class<? extends ITreeStore> treeStoreImpl = NoImplTreeStore.class;

    /**
     * 认证服务实现类
     */
    private Class<? extends IAuthRemoteService> authRemoteServiceImpl = DefaultAuthRemoteServiceImpl.class;

    /**
     * 用户服务实现类
     */
    private Class<? extends IUserRemoteService> userRemoteServiceImpl = DefaultUserRemoteServiceImpl.class;
}
