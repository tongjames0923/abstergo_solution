package com.tbs.xxl.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "tbs.xxl-job")
@Data
public class XxlJobProperty {

    /**
     * 是否启用
     */
    boolean enable;

    /**
     * 是否启用json任务执行器
     */
    boolean enableJsonJobExecutor;
}
