package com.tbs.sql.annotation.condition;

import java.lang.annotation.*;

/**
 * 查询字段顺序索引注解。
 *
 * <p>该注解用于控制WHERE子句中查询条件的顺序。通过设置索引值，可以确保查询条件
 * 按照指定的顺序生成，索引值越小，条件越靠前。</p>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>需要精确控制WHERE子句中条件顺序时</li>
 *   <li>需要确保某些查询条件优先执行时</li>
 *   <li>优化查询性能时</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * public class UserQuery implements IQueryCriteria {
 *     @QueryIndex(index = 1)  // 第一个条件
 *     @QueryMapping(value = "status")
 *     @QueryField(contrast = ContrastEnum.EQUAL)
 *     private Integer status;
 *     
 *     @QueryIndex(index = 2)  // 第二个条件
 *     @QueryMapping(value = "name")
 *     @QueryField(contrast = ContrastEnum.LIKE)
 *     private String nameLike;
 *     
 *     // 未设置index的字段默认为1000，排在最后
 *     @QueryMapping(value = "age")
 *     @QueryField(contrast = ContrastEnum.GREATER_EQUAL)
 *     private Integer minAge;
 * }
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>索引值越小，条件越靠前</li>
 *   <li>未设置索引的字段默认值为1000</li>
 *   <li>相同索引值的字段按声明顺序排列</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see QueryField
 * @see QueryMapping
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface QueryIndex {
    int index() default 1000;
}
