package com.tbs.sql.annotation.condition;

import java.lang.annotation.*;

/**
 * 字段间或连接符标记注解。
 *
 * <p>该注解用于标记查询条件字段之间的逻辑连接关系为OR。
 * 默认情况下，多个查询条件使用AND连接，使用此注解可以将条件改为OR连接。</p>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>需要构建OR条件的查询</li>
 *   <li>多个字段满足任一条件即可的查询场景</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * public class UserQuery implements IQueryCriteria {
 *     @QueryMapping(value = "name")
 *     @QueryField(contrast = ContrastEnum.LIKE)
 *     private String nameLike;
 *     
 *     @OrField  // 标记此字段与前一个字段使用OR连接
 *     @QueryMapping(value = "email")
 *     @QueryField(contrast = ContrastEnum.LIKE)
 *     private String emailLike;
 * }
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>该注解只能用于字段级别</li>
 *   <li>需要配合 {@link QueryField} 注解使用</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see QueryField
 * @see com.tbs.sql.enums.ConnectorEnum
 */
@Target(ElementType.FIELD)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface OrField {
}
