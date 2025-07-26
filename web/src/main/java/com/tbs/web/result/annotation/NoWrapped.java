package com.tbs.web.result.annotation;

import java.lang.annotation.*;

/**
 * 排除统一返回结果注解
 *
 * @author tongj
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface NoWrapped {
}
