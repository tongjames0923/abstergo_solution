package com.tbs.sql.annotation.condition;

import java.lang.annotation.*;

/**
 * 字段间或连接符标记
 *
 * @author abstergo
 * @since 1.0.0
 */
@Target(ElementType.FIELD)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface OrField {
}
