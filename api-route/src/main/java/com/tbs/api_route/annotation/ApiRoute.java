package com.tbs.api_route.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.tbs.api_route.IApiRouter;

/**
 * 接口路由注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiRoute {
	Class<? extends IApiRouter> value();
}
