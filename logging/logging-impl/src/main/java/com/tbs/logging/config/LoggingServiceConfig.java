package com.tbs.logging.config;

import com.tbs.app.service.log.ISystemLogService;
import com.tbs.logging.core.ILogExtractor;
import com.tbs.logging.impl.DbLogExtractor;
import com.tbs.logging.impl.SimpleSystemLogServiceImpl;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggingServiceConfig {
    @Bean
    public ILogExtractor logExtractor() {
        return new DbLogExtractor();
    }

    @Bean
    @DubboService
    public ISystemLogService logService() {
        return new SimpleSystemLogServiceImpl();
    }
}
