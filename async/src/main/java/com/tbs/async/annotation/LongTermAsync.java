package com.tbs.async.annotation;

import com.tbs.thread.IOnThreadError;
import com.tbs.thread.impl.DefaultOnThreadError;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 长期异步
 *
 * @author abstergo
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface LongTermAsync {
    /**
     * 线程名称
     *
     * @return
     */
    String value() default "";

    /**
     * 线程异常处理
     *
     * @return
     */
    Class<? extends IOnThreadError> onThreadError() default DefaultOnThreadError.class;
}
