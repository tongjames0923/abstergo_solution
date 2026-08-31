package com.tbs.web.result.annotation;

import java.lang.annotation.*;

/**
 * 排除统一返回结果注解
 * <p>
 * 标注在控制器方法上，使其返回值不经过 {@code ResponseAjaxAspect} 的统一包装，直接原样返回。
 * 适用于文件下载、原生 JSON 接口等不需要 {@link AjaxResult} 包裹的场景。
 * </p>
 *
 * @author tongj
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface NoWrapped {
}
