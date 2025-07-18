package com.tbs.sql.annotation.dialect;

import com.tbs.sql.config.MysqlDialectConfig;
import org.springframework.context.annotation.Import;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * mysql的方言功能启用
 *
 * @author abstergo
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({MysqlDialectConfig.class})
public @interface MYSQL {
}
