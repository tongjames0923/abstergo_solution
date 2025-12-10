package com.tbs.sql.annotation;

import com.tbs.sql.config.SqlConfig;
import com.tbs.sql.properties.SqlProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 启用TBS SQL工具注解
 * <p>
 * 用于启用TBS SQL工具的自动配置和功能
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */

@EnableTransactionManagement
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({SqlConfig.class})
@EnableConfigurationProperties({SqlProperty.class})
public @interface EnableTbsSqlUtils {
}
