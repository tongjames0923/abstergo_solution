package com.tbs.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author abstergo
 */
@Data
@Component
@ConfigurationProperties(prefix = "tbs.system.sql")
public class SystemSqlProperty {
    /**
     * sql文件前缀
     */
    private String sqlPrefix = "sql/";
    /**
     * sql文件后缀
     */
    private String sqlSuffix = ".sql";

}
