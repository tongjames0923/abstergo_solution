package com.tbs.sql.annotation.mapping;

import com.tbs.sql.annotation.condition.QueryField;

import java.lang.annotation.*;

/**
 * 查询字段映射名称注解。
 *
 * <p>该注解用于指定查询字段与数据库列名的映射关系。当查询类中的字段名与数据库列名
 * 不一致时，使用此注解可以指定正确的映射关系。</p>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>查询字段名与数据库列名不一致时</li>
 *   <li>需要使用别名进行查询时</li>
 *   <li>需要映射到特定数据库字段时</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * public class UserQuery implements IQueryCriteria {
 *     // 查询类字段名name映射到数据库字段user_name
 *     @QueryMapping(value = "user_name")
 *     @QueryField(contrast = ContrastEnum.LIKE)
 *     private String nameLike;
 *
 *     // 映射到数据库字段create_time
 *     @QueryMapping(value = "create_time")
 *     @QueryField(contrast = ContrastEnum.GREATER_EQUAL)
 *     private Date startTime;
 * }
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>该注解只能用于字段级别</li>
 *   <li>需要配合 {@link QueryField} 注解使用</li>
 *   <li>映射名称应该是数据库中实际存在的字段名</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see QueryField
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface QueryMapping {
    String value() default "";
}
