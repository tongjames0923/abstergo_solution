package com.tbs.logging.config;

import com.tbs.shared.interfaces.log.ISystemOperateLogSender;
import com.tbs.shared.interfaces.log.ISystemOperateLogViewService;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "tbs.logging")
@Data
@Component
public class LoggingProperty {

    /**
     * 日志查看服务类
     */
    private Class<? extends ISystemOperateLogViewService> systemOperateLogViewService;

    /**
     * 日志记录服务类
     */
    private Class<? extends ISystemOperateLogSender> systemOperateLogSender;
}
