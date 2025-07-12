package com.tbs.cache.annotation;

import com.tbs.cache.core.IExpiredActuator;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 缓存失效时间
 *
 * @author tongj
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExpireOn {

    /**
     * 失效时间
     *
     * @return
     */
    long value() default 0;

    /**
     * 缓存失效时间单位
     *
     * @return
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 缓存名称
     *
     * @return
     */
    String[] cacheNames() default {};

    /**
     * 是否失效所有缓存
     *
     * @return
     */
    boolean expireAll() default false;
}
