package com.tbs.sql.annotation.condition;

import com.tbs.sql.annotation.mapping.QueryMapping;
import com.tbs.sql.enums.ConnectorEnum;
import com.tbs.sql.enums.ContrastEnum;
import com.tbs.sql.provider.ICustomWhereProvider;

import java.lang.annotation.*;

/**
 * 查询字段注解，用于定义字段的查询条件。
 *
 * <p>该注解是构建SQL查询条件的核心注解，通过它可以定义字段的比较方式、逻辑连接符、
 * 是否忽略大小写、是否忽略空值等查询条件。</p>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>定义实体查询条件</li>
 *   <li>构建动态查询语句</li>
 *   <li>实现多条件组合查询</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * public class UserQuery implements IQueryCriteria {
 *     // 等值查询，忽略空值
 *     @QueryMapping(value = "username")
 *     @QueryField(contrast = ContrastEnum.EQUAL, ignoreNull = true)
 *     private String username;
 *
 *     // 模糊查询，忽略大小写
 *     @QueryMapping(value = "name")
 *     @QueryField(contrast = ContrastEnum.LIKE, ignoreCase = true)
 *     private String searchKey;
 *
 *     // IN查询
 *     @QueryMapping(value = "status")
 *     @QueryField(contrast = ContrastEnum.IN)
 *     private List<Integer> statusList;
 *
 *     // 自定义查询条件
 *     @QueryMapping(value = "age")
 *     @QueryField(contrast = ContrastEnum.GREATER_EQUAL, customWhereProvider = CustomAgeProvider.class)
 *     private Integer minAge;
 * }
 * }</pre>
 *
 * <h3>参数说明</h3>
 * <ul>
 *   <li>{@code index}: 同一字段查询顺序，升序，用于控制WHERE子句中条件的顺序</li>
 *   <li>{@code connector}: 逻辑连接符，默认AND，可选OR</li>
 *   <li>{@code contrast}: 比较运算符，如EQUAL、LIKE、IN等</li>
 *   <li>{@code ignoreCase}: 是否忽略大小写，默认false</li>
 *   <li>{@code ignoreNull}: 是否忽略空值，默认true，当字段值为null时不生成查询条件</li>
 *   <li>{@code customWhereProvider}: 自定义查询条件提供者，用于实现复杂的查询逻辑</li>
 * </ul>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>该注解支持重复使用，通过 {@link QueryFields} 容器注解实现</li>
 *   <li>需要配合 {@link QueryMapping} 注解指定映射字段名</li>
 *   <li>当 {@code ignoreNull=true} 时，字段值为null不会生成查询条件</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see QueryMapping
 * @see QueryFields
 * @see com.tbs.sql.enums.ContrastEnum
 * @see com.tbs.sql.enums.ConnectorEnum
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
@Repeatable(QueryFields.class)
public @interface QueryField {
    /** 同一字段查询顺序，升序 */
    int index() default -1;
    /** 关系运算 */
    ConnectorEnum connector() default ConnectorEnum.AND;
    /** 比较运算 */
    ContrastEnum contrast() default ContrastEnum.EQUAL;
    /** 是否忽略大小写 */
    boolean ignoreCase() default false;
    /** 是否忽略空值 */
    boolean ignoreNull() default true;
    /** 自定义查询条件提供者 */
    Class<? extends ICustomWhereProvider> customWhereProvider() default ICustomWhereProvider.class;
}
