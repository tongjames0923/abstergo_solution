package com.tbs.cache.serialize.annotation;

import com.tbs.common.data.IDataBridge;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheTransform {
    Class<? extends IDataBridge> bridgeType();

    String bridgeName() default "";
}
