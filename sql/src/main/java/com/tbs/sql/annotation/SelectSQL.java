package com.tbs.sql.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 直接使用的SQL查询语句
 *
 * @author abstergo
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
@Component
public @interface SelectSQL {
    String value() default "";
}
