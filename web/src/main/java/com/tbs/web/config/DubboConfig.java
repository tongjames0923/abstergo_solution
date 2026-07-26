package com.tbs.web.config;

import com.alibaba.dubbo.common.URL;
import org.apache.dubbo.registry.client.ServiceDiscovery;
import org.apache.dubbo.registry.client.ServiceDiscoveryFactory;
import org.apache.dubbo.rpc.model.ApplicationModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DubboConfig {
    @Bean
    ServiceDiscovery dubboServiceDiscovery(@Value("${dubbo.registry.address}") String registryAddress) {
        ApplicationModel applicationModel = ApplicationModel.defaultModel();
        ServiceDiscoveryFactory factory =
            applicationModel.getExtensionLoader(ServiceDiscoveryFactory.class).getExtension("zookeeper");
        ServiceDiscovery serviceDiscovery = factory.getServiceDiscovery(URL.valueOf(registryAddress));
        return serviceDiscovery;
    }
}
