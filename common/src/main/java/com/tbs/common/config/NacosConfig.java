package com.tbs.common.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NacosConfig {

    @Value("${spring.cloud.nacos.config.server-addr:127.0.0.1:8848}")
    String nacosConfigServerAddr;

    @Bean
    @ConditionalOnMissingBean(ConfigService.class)
    ConfigService configService() throws NacosException {
        
        return NacosFactory.createConfigService(nacosConfigServerAddr);
    }
}
