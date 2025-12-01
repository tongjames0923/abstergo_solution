package com.tbs.web.annotation;

import com.tbs.common.annotation.EnableCommonPack;
import com.tbs.web.config.WebAutoRegister;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import java.lang.annotation.*;

/**
 * 启用web包
 *
 * @author tongj
 */
@Import(WebAutoRegister.class)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@EnableCommonPack
@Target(ElementType.TYPE)
public @interface EnableWebPack {
}
