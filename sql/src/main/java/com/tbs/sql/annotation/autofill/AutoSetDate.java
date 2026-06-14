package com.tbs.sql.annotation.autofill;

import com.tbs.sql.provider.impl.DateValueAutoProvider;

import java.lang.annotation.*;

/**
 * 自动设置参数日期注解，支持Date和LocalDateTime
 *
 * @author abstergo
 * @since 1.0.0
 */
@SqlAutoValue(value = DateValueAutoProvider.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AutoSetDate {
}
