package com.tbs.common.provider.vo;

import com.tbs.common.provider.IRegisterContributor;
import com.tbs.common.provider.IRegistryCenter;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InstanceProxy<T> {
    /**
     * 注册中心
     */
    private IRegistryCenter registryCenter;
    /**
     * 注册贡献者
     */
    private IRegisterContributor contributor;
    /**
     * 服务实例
     */
    private T instance;
    /**
     * 服务版本
     */
    private String version;
    /**
     * 业务线
     */
    private String business;
}
