package com.tbs.cache.annotation;

import com.tbs.cache.config.CachePackAutoRegister;
import com.tbs.common.annotation.EnableCommonPack;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用缓存包
 *
 * @author tongj
 */
@EnableCommonPack
@Inherited
@Import(CachePackAutoRegister.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnableTbsCache {
}
