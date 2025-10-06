package com.tbs.common.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "tbs.start-up")
@Data
public class StartUpProperty {
    /**
     * 是否启用
     */
    private boolean enabled = true;
    /**
     * 是否异步执行
     */
    private boolean async = true;
    /**
     * 是否执行可选项
     */
    private boolean optionalExecute = true;
}
