package com.tbs.common.provider.vo;

import com.tbs.common.provider.IRegisterContributor;
import com.tbs.common.provider.IRegistryCenter;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InstanceProxy {
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
    private Object instance;
    /**
     * 服务版本
     */
    private String version;
    /**
     * 业务线
     */
    private String business;

    /**
     * 目标类型全限定名
     */
    private String targetTypeFullName;

    /**
     * 接口全限定名
     */
    private String interfaceFullName;
}
