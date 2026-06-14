package com.tbs.sql.annotation.core;

import java.lang.annotation.*;

/**
 * 直接使用的SQL查询语句
 *
 * @author abstergo
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface SelectSQL {
    String value() default "";
}
