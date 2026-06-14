package com.tbs.sql.annotation.core;

import com.tbs.sql.autoconfig.SqlAutoConfiguration;
import com.tbs.sql.annotation.dialect.UseMysql;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 启用TBS SQL工具注解
 *
 * @author abstergo
 * @since 2.0.0
 */
@EnableTransactionManagement
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({SqlAutoConfiguration.class})
@EnableConfigurationProperties({com.tbs.sql.properties.SqlProperty.class})
public @interface EnableTbsSql {
}
