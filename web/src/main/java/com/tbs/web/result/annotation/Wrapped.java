package com.tbs.web.result.annotation;

import com.tbs.web.result.IResultCreator;
import com.tbs.web.result.impl.DefaultResultCreateor;

import java.lang.annotation.*;

/**
 * 统一返回结果注解
 * <p>
 * 标注在控制器方法上，声明其返回值交由 {@link IResultCreator} 包装为统一 {@link AjaxResult}。
 * 可由 {@code value} 指定具体的包装策略实现，默认使用 {@link DefaultResultCreateor}。
 * </p>
 *
 * @author tongj
 * @see IResultCreator 结果创建契约
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Wrapped {
    /**
     * 构建结果提供者
     *
     * @return 实现 {@link IResultCreator} 的类，默认 {@link DefaultResultCreateor}
     */
    Class<? extends IResultCreator> value() default DefaultResultCreateor.class;
}
