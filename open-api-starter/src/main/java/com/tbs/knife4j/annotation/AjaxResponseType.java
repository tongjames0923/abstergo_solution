package com.tbs.knife4j.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注Ajax接口响应中data字段的实际类型
 * <p>
 * 用于在OpenAPI/Knife4j文档中准确描述返回值结构。
 * 通过注解的Class类型引用（运行时保留）规避Java泛型擦除问题，
 * 由 {@code AjaxResponseTypeOperationCustomizer} 在文档生成阶段读取并构建响应Schema。
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * // 简单对象
 * {@code @AjaxResponseType(DicModel.class)}
 * public AjaxResult query(...) { ... }
 *
 * // 集合类型
 * {@code @AjaxResponseType(value = List.class, genericArgs = {DicModel.class})}
 * public AjaxResult listAll(...) { ... }
 *
 * // 无data返回（纯操作类接口）
 * {@code @AjaxResponseType(void.class)}
 * public AjaxResult enable(...) { ... }
 * </pre>
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 * @see com.tbs.knife4j.config.AjaxResponseTypeOperationCustomizer
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AjaxResponseType {

    /**
     * data字段的实际类型
     * <p>
     * 特殊值 {@code void.class} 表示响应中不包含data字段（如纯操作类接口）
     * </p>
     */
    Class<?> value();

    /**
     * 泛型参数类型
     * <p>
     * 用于构建集合、Map等泛型类型的data schema：
     * <ul>
     *     <li>{@code List.class} + genericArgs = {DicModel.class} → data为DicModel数组</li>
     *     <li>{@code Map.class} + genericArgs = {String.class, DicModel.class} → data为key为String、value为DicModel的对象</li>
     * </ul>
     * 普通类型不需要设置此属性
     * </p>
     */
    Class<?>[] genericArgs() default {};
}
