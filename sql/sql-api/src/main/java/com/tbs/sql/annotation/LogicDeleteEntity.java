package com.tbs.sql.annotation;

import java.lang.annotation.*;

/**
 * @author abstergo
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface LogicDeleteEntity {

    /**
     * 激活状态的值
     */
    int[] activeFlagValues() default {1};

    /**
     * 字段名
     */
    String filedName() default "flag";

    /**
     * 当字段为空时，是否视作被删除
     */
    boolean onNullIsDeleted() default true;
}
