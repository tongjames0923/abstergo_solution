package com.tbs.common.config;

import cn.hutool.extra.spring.SpringUtil;
import com.tbs.common.IStartup;
import com.tbs.common.config.property.StartUpProperty;
import com.tbs.common.util.CollUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.Collection;

@Configuration
@Slf4j
public class StartUpConfig {

    @Resource
    private StartUpProperty startUpProperty;

    private void realStartUp() {
        Collection<IStartup> startups = null;
        try {
            startups = SpringUtil.getBeansOfType(IStartup.class).values();
        } catch (Exception e) {
            log.error("startup error", e);
        }
        if (CollUtil.isEmpty(startups)) {
            return;
        }

        boolean optionalExecute = startUpProperty.isOptionalExecute();
        startups.stream().filter(a -> a != null).sorted((a1, a2) -> {
            return a1.getOrder() - a2.getOrder();
        }).forEach(startup -> {
            if (!optionalExecute && startup.isOptional()) {
                log.info("skip optional startup {}", startup.getClass().getSimpleName());
                return;
            }
            log.info("starting {}", startup.getClass().getSimpleName());
            try {
                startup.startUp();
            } catch (Exception e) {
                log.error("startup error", e);
            } finally {
                log.info("startup {} completed", startup.getClass().getSimpleName());
            }

        });
    }

    @Bean
    @ConditionalOnProperty(value = "tbs.start-up.enabled", havingValue = "true", matchIfMissing = true)
    ApplicationRunner startUp() {
        return new ApplicationRunner() {
            @Override
            public void run(ApplicationArguments args) throws Exception {
                if (startUpProperty.isAsync()) {
                    log.info("startup async");
                    Thread thread = new Thread(() -> {
                        realStartUp();
                    });
                    thread.start();
                } else {
                    log.info("startup sync");
                    realStartUp();
                }
                log.info("startup totally completed");
            }
        };
    }
}
