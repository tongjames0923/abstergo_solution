package com.tbs.common.config;

import cn.hutool.extra.spring.SpringUtil;
import com.tbs.common.IStartup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.stream.Collectors;

@Configuration
@Slf4j
public class StartUpConfig {
    @Bean
    ApplicationRunner startUp() {
        return new ApplicationRunner() {

            @Override
            public void run(ApplicationArguments args) throws Exception {
                for (IStartup startup : SpringUtil.getBeansOfType(IStartup.class).values().stream().sorted((a1, a2) -> {
                    return a1.getOrder() - a2.getOrder();
                }).collect(Collectors.toList())) {
                    if (startup != null) {
                        log.info("{} instance[{}] started", startup.getClass().getSimpleName(), startup);
                        startup.startUp();
                    }
                }
            }
        };
    }
}
