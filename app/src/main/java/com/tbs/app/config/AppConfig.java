package com.tbs.app.config;

import com.tbs.common.IErrorThrowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author tongj
 */
@Configuration
public class AppConfig {
    @Bean
    IErrorThrowEvent errorThrowEvent() {
        return new IErrorThrowEvent() {
            private Logger logger = LoggerFactory.getLogger(AppConfig.class);

            @Override
            public boolean handle(Throwable throwable) {
                logger.debug("will throw error {}", throwable.getMessage());
                return false;
            }
        };
    }
}
