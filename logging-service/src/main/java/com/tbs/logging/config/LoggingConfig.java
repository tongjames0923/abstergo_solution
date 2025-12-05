package com.tbs.logging.config;

import com.tbs.logging.impl.DbSystemOpLog;
import com.tbs.shared.interfaces.log.ISystemOperateLog;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggingConfig {

    public static final String SYSTEM_OPERATE_LOG_BEAN_NAME = "systemOperateLog";

    @Bean(SYSTEM_OPERATE_LOG_BEAN_NAME)
    ISystemOperateLog systemOperateLog() {
        return new DbSystemOpLog();
    }

}
