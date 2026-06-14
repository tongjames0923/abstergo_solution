package com.tbs.sql.annotation.condition;

import java.lang.annotation.*;

/**
 * 查询字段容器注解
 *
 * @author abstergo
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface QueryFields {
    QueryField[] value();
}
