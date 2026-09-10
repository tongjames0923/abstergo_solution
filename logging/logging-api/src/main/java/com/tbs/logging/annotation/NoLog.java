package com.tbs.logging.annotation;

import java.lang.annotation.*;

/**
 * 不进行日志记录注解
 * @author tbs
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface NoLog {
}
