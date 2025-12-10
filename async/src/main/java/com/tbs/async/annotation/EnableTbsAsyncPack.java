package com.tbs.async.annotation;

import com.tbs.async.config.AsyncAutoRegister;
import com.tbs.common.annotation.EnableCommonPack;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用异步包注解
 * <p>
 * 使用该注解可以自动注册和启用async包中的异步功能组件
 * 通过@Import导入AsyncAutoRegister类，实现自动包扫描和Bean注册
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Import(AsyncAutoRegister.class)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@EnableCommonPack
@Target(ElementType.TYPE)
public @interface EnableTbsAsyncPack {
    /**
     * 是否使用广播模块
     * <p>
     * 设置为true时，将启用事件广播功能
     * </p>
     *
     * @return true 使用广播模块，false 不使用广播模块
     */
    boolean useBroadcast() default true;
}
