package com.tbs.app.annotation;

import java.lang.annotation.*;

/**
 * @author tbs
 * @date 2023/5/15
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface NoLog {
}
