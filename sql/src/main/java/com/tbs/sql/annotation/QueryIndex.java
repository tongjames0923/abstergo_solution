package com.tbs.sql.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 设置查询where语句字段的顺序，升序
 *
 * @author abstergo
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
@Component
public @interface QueryIndex {

    /**
     * Index int.
     *
     * @return the int
     */
    int index() default 1000;
}
