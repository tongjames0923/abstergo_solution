package com.tbs.sql.annotation;

import com.tbs.sql.provider.IAutoValueProvider;
import org.springframework.stereotype.Component;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 自动设置参数注解
 *
 * @author abstergo
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface SqlAutoValue {
    Class<? extends IAutoValueProvider> value() default IAutoValueProvider.class;

}
