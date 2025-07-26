package com.tbs.web.result.annotation;

import com.tbs.web.result.IAjaxBuilderProvider;
import com.tbs.web.result.IResultCreator;
import com.tbs.web.result.impl.DefaultResultCreateor;
import com.tbs.web.result.impl.SimpleBuilderProvider;

import java.lang.annotation.*;

/**
 * 统一返回结果注解
 *
 * @author tongj
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Wrapped {
    /**
     * 构建结果提供者
     *
     * @return
     */
    Class<? extends IResultCreator> value() default DefaultResultCreateor.class;
}
