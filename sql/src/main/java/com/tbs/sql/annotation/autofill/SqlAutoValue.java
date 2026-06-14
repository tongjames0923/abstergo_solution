package com.tbs.sql.annotation.autofill;

import com.tbs.sql.provider.IAutoValueProvider;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * SQL自动填充值注解
 *
 * @author abstergo
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface SqlAutoValue {
    Class<? extends IAutoValueProvider> value() default IAutoValueProvider.class;
}
