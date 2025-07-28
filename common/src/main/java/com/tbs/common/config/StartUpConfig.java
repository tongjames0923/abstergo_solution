package com.tbs.common.config;

import cn.hutool.extra.spring.SpringUtil;
import com.tbs.common.IStartup;
import com.tbs.common.util.CollUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

@Configuration
@Slf4j
public class StartUpConfig {
    @Bean
    ApplicationRunner startUp() {
        return new ApplicationRunner() {

            @Override
            public void run(ApplicationArguments args) throws Exception {
                Collection<IStartup> startups = null;
                try {
                    startups = SpringUtil.getBeansOfType(IStartup.class).values();
                } catch (Exception e) {
                    log.error("startup error", e);
                }
                if (CollUtil.isEmpty(startups)) {
                    return;
                }
                startups.stream().filter(a -> a != null).sorted((a1, a2) -> {
                    return a1.getOrder() - a2.getOrder();
                }).forEach(startup -> {
                    log.info("{} instance[{}] started", startup.getClass().getSimpleName(), startup);
                    startup.startUp();
                });
            }
        };
    }
}
