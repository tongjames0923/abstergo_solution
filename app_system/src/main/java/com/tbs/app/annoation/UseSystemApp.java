package com.tbs.app.annoation;

import com.tbs.app.config.SystemAppConfig;
import com.tbs.app.config.SystemAppProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 使用系统应用
 *
 * @author abstergo
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(SystemAppConfig.class)
@EnableConfigurationProperties(SystemAppProperty.class)
public @interface UseSystemApp {
}
