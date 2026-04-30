package com.tbs.logging.config;

import com.tbs.logging.impl.DbSystemOpLogService;
import com.tbs.shared.interfaces.log.ISystemOperateLogViewService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggingConfig {

    public static final String SYSTEM_OPERATE_LOG_BEAN_NAME = "systemOperateLog";
    public static final String SYSTEM_OPERATE_LOG_VIEW_BEAN_NAME = "systemOperateLogView";

    @Bean(SYSTEM_OPERATE_LOG_BEAN_NAME)
    ISystemOperateLogViewService systemOperateLog() {
        return new DbSystemOpLogService();
    }

}
