package com.tbs.sql.annotation.condition;

import java.lang.annotation.*;

/**
 * 设置查询where语句字段的顺序，升序
 *
 * @author abstergo
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface QueryIndex {
    int index() default 1000;
}
