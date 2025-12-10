package com.tbs.async.annotation;

import com.tbs.thread.IOnThreadError;
import com.tbs.thread.impl.DefaultOnThreadError;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 长期异步注解
 * <p>
 * 用于标记需要在长期运行的线程中执行的方法
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface LongTermAsync {
    /**
     * 线程名称
     * <p>
     * 指定异步执行时使用的线程名称
     * </p>
     *
     * @return 线程名称，默认为空字符串
     */
    String value() default "";

    /**
     * 线程异常处理类
     * <p>
     * 指定异步执行过程中发生异常时的处理类
     * </p>
     *
     * @return 异常处理类，默认使用DefaultOnThreadError
     */
    Class<? extends IOnThreadError> onThreadError() default DefaultOnThreadError.class;
}
