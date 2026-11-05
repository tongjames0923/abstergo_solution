package com.tbs.sql.annotation.order;

import com.tbs.sql.constants.OrderConstants;

import java.lang.annotation.*;

/**
 * 查询排序字段注解。
 *
 * <p>该注解用于定义查询结果的排序规则，可以指定排序字段和排序方向（升序或降序）。
 * 支持多个字段排序，按注解声明顺序依次应用。</p>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>需要对查询结果进行排序时</li>
 *   <li>需要多字段组合排序时</li>
 *   <li>需要动态排序时</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * public class UserQuery implements IQueryCriteria {
 *     // 按创建时间升序排序
 *     @QueryOrderField(mappedName = "create_time", order = OrderConstants.ASC)
 *     private Boolean orderByCreateTime;
 *     
 *     // 按更新时间降序排序
 *     @QueryOrderField(mappedName = "update_time", order = OrderConstants.DESC)
 *     private Boolean orderByUpdateTime;
 * }
 * }</pre>
 *
 * <h3>参数说明</h3>
 * <ul>
 *   <li>{@code mappedName}: 排序字段的映射名称，对应数据库中的列名</li>
 *   <li>{@code order}: 排序方向，可选值为 {@link OrderConstants#ASC}（升序）和 {@link OrderConstants#DESC}（降序）</li>
 * </ul>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>该注解只能用于字段级别</li>
 *   <li>字段值为null时不会应用排序</li>
 *   <li>可以配合 {@link com.tbs.sql.order.IQueryOrderBy} 接口实现自定义排序逻辑</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see OrderConstants
 * @see com.tbs.sql.order.IQueryOrderBy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Documented
public @interface QueryOrderField {
    String mappedName() default "";
    String order() default OrderConstants.ASC;
}
