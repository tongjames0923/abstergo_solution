package com.tbs.shared.annotation;

import com.tbs.common.annotation.EnableCommonPack;
import com.tbs.shared.config.SharedAutoRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 启用共享库bean
 * @author tongj
 */
@EnableCommonPack
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.TYPE)
@Inherited
@Documented
@Import(SharedAutoRegister.class)
public @interface EnableSharedLibBean {
}
