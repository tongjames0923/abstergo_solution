package com.tbs.web.config;

import com.alibaba.dubbo.common.URL;
import org.apache.dubbo.registry.client.ServiceDiscovery;
import org.apache.dubbo.registry.nacos.NacosServiceDiscovery;
import org.apache.dubbo.rpc.model.ApplicationModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DubboConfig {
    @Bean
    ServiceDiscovery dubboServiceDiscovery(@Value("${dubbo.registry.address}") String registryAddress) {
        ApplicationModel applicationModel = ApplicationModel.defaultModel();
        return new NacosServiceDiscovery(applicationModel, URL.valueOf(registryAddress));
    }
}
