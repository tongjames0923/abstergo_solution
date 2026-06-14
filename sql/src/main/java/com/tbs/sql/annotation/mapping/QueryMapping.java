package com.tbs.sql.annotation.mapping;

import java.lang.annotation.*;

/**
 * 映射名称注解
 *
 * @author abstergo
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface QueryMapping {
    String value() default "";
}
