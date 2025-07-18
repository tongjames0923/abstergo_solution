package com.tbs.sql.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 映射名称注解
 *
 * @author tongj
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
@Component
public @interface QueryMapping {
    String value() default "";
}
