package com.tbs.cache.annotation;

import com.tbs.cache.config.CachePackAutoRegister;
import com.tbs.common.annotation.EnableCommonPack;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用缓存包注解
 * <p>
 * 用于自动注册和启用cache包中的缓存功能组件
 * 通过@Import导入CachePackAutoRegister实现自动扫描和Bean注册
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
@EnableCommonPack
@Inherited
@Import(CachePackAutoRegister.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnableTbsCache {
}
