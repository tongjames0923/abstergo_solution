package com.tbs.sql.annotation.condition;

import java.lang.annotation.*;

/**
 * 查询字段容器注解，用于存储重复的 {@link QueryField} 注解。
 *
 * <p>该注解是 {@link QueryField} 的容器注解，当同一个字段需要多个查询条件时，
 * Java 编译器会自动将多个 {@link QueryField} 注解包装到此容器中。</p>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>同一字段需要多个查询条件时</li>
 *   <li>需要构建复杂的组合查询条件时</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * public class UserQuery implements IQueryCriteria {
 *     // 同一字段的多个查询条件会自动包装到QueryFields容器中
 *     @QueryField(contrast = ContrastEnum.GREATER_EQUAL)
 *     @QueryField(contrast = ContrastEnum.LESS_EQUAL)
 *     private Integer age;
 * }
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>该注解通常由编译器自动生成，不需要手动使用</li>
 *   <li>如果需要手动使用，请确保正确设置 {@code value()} 属性</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see QueryField
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface QueryFields {
    QueryField[] value();
}
