package com.tbs.sql.annotation;

import com.tbs.sql.provider.impl.DateValueAutoProvider;
import org.springframework.stereotype.Component;


import java.lang.annotation.*;

/**
 * 自动设置参数日期注解，支持Date和LocalDateTime
 *
 * @author abstergo
 */
@SqlAutoValue(value = DateValueAutoProvider.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Component
@Target(ElementType.FIELD)
public @interface AutoSetDate {
}
