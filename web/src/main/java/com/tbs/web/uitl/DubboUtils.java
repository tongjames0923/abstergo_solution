package com.tbs.web.uitl;

import com.tbs.common.util.framework.SpringUtil;
import org.apache.dubbo.registry.client.ServiceDiscovery;
import org.apache.dubbo.registry.client.ServiceInstance;

import java.util.Collection;
import java.util.List;

/**
 * Dubbo 工具类
 * <p>
 * 基于注册中心 {@link ServiceDiscovery} 提供 Dubbo 服务发现能力：列出全部服务名、按名获取实例列表。
 * 供泛化调用等场景在运行时解析远程服务实例。
 * </p>
 */
public class DubboUtils {

    /**
     * 获取所有dubbo服务名称
     *
     * @return
     */
    public static Collection<String> getServiceNames() {
        return SpringUtil.getBean(ServiceDiscovery.class).getServices();
    }

    /**
     * 根据服务名称获取服务实例
     *
     * @param serviceName
     * @return
     */
    public static List<ServiceInstance> getServiceInstance(String serviceName) {
        return SpringUtil.getBean(ServiceDiscovery.class).getInstances(serviceName);
    }

}
