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
 * @author abstergo
 */

@EnableTransactionManagement
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({SqlConfig.class})
@EnableConfigurationProperties({SqlProperty.class})
public @interface EnableTbsSqlUtils {
}
