package com.tbs.sql.annotation.order;

import com.tbs.sql.constants.OrderConstants;

import java.lang.annotation.*;

/**
 * 字段排序顺序标记
 *
 * @author abstergo
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Documented
public @interface QueryOrderField {
    String mappedName() default "";
    String order() default OrderConstants.ASC;
}
