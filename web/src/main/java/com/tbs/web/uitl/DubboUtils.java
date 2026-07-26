package com.tbs.web.uitl;

import com.tbs.common.util.framework.SpringUtil;
import org.apache.dubbo.registry.client.ServiceDiscovery;
import org.apache.dubbo.registry.client.ServiceInstance;

import java.util.Collection;
import java.util.List;

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
