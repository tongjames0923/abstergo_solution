package com.tbs.sql.annotation.dialect;

import com.tbs.sql.autoconfig.MysqlDialectAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 启用MySQL方言功能
 *
 * @author abstergo
 * @since 2.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({MysqlDialectAutoConfiguration.class})
public @interface UseMysql {
}
