package com.tbs.common.annotation;

import com.tbs.common.config.CommonPackAutoRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用通用包注解
 * <p>
 * 使用该注解可以自动注册和启用common包中的功能组件
 * 通过@Import导入CommonPackAutoRegister类，实现自动包扫描和Bean注册
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Import(CommonPackAutoRegister.class)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(ElementType.TYPE)
public @interface EnableCommonPack {
}
