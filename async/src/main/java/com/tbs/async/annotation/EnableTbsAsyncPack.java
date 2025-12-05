package com.tbs.async.annotation;

import com.tbs.async.config.AsyncAutoRegister;
import com.tbs.common.annotation.EnableCommonPack;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用异步包
 * @author abstergo
 */
@Import(AsyncAutoRegister.class)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@EnableCommonPack
@Target(ElementType.TYPE)
public @interface EnableTbsAsyncPack {
    /**
     * 是否使用广播模块
     *
     * @return true 使用广播模块
     */
    boolean useBroadcast() default true;
}
