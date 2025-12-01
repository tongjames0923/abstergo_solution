package com.tbs.common.annotation;

import com.tbs.common.CommonPackAutoRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用通用包
 */
@Import(CommonPackAutoRegister.class)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(ElementType.TYPE)
public @interface EnableCommonPack {
}
