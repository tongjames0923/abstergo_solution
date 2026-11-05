package com.tbs.sql.enums;

/**
 * 查询对比枚举类，定义了查询条件中可用的各种比较操作符。
 *
 * <p>该枚举类定义了SQL查询中使用的比较操作符，包括等值、范围、模糊、IN等多种类型。
 * 用于在查询条件中指定字段与值的比较方式。</p>
 *
 * <h3>枚举值说明</h3>
 * <ul>
 *   <li>{@link #EQUAL}: 等于，对应SQL中的=操作符</li>
 *   <li>{@link #NOT_EQUAL}: 不等于，对应SQL中的!=或<>操作符</li>
 *   <li>{@link #GREATER}: 大于，对应SQL中的>操作符</li>
 *   <li>{@link #LESS}: 小于，对应SQL中的<操作符</li>
 *   <li>{@link #GREATER_EQUAL}: 大于等于，对应SQL中的>=操作符</li>
 *   <li>{@link #LESS_EQUAL}: 小于等于，对应SQL中的<=操作符</li>
 *   <li>{@link #LIKE}: 模糊匹配，对应SQL中的LIKE操作符</li>
 *   <li>{@link #LLIKE}: 左模糊匹配，对应SQL中的LIKE 'value%'</li>
 *   <li>{@link #RLIKE}: 右模糊匹配，对应SQL中的LIKE '%value'</li>
 *   <li>{@link #NOT_LIKE}: 不匹配，对应SQL中的NOT LIKE操作符</li>
 *   <li>{@link #IN}: 在集合中，对应SQL中的IN操作符</li>
 *   <li>{@link #NOT_IN}: 不在集合中，对应SQL中的NOT IN操作符</li>
 *   <li>{@link #IS_NULL}: 为空，对应SQL中的IS NULL操作符</li>
 *   <li>{@link #IS_NOT_NULL}: 不为空，对应SQL中的IS NOT NULL操作符</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>在 {@link com.tbs.sql.annotation.condition.QueryField} 注解中指定比较方式</li>
 *   <li>在DSL查询中使用对应的比较方法</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 在注解中使用
 * @QueryField(contrast = ContrastEnum.EQUAL)
 * private String field1;
 * 
 * // 在DSL中使用
 * wrapper.query()
 *     .where()
 *     .equal(User::getStatus, 1)
 *     .like(User::getName, "test")
 *     .in(User::getAge, Arrays.asList(18, 20, 22))
 *     .pour()
 *     .fetch();
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>根据字段类型选择合适的比较操作符</li>
 *   <li>IN操作符需要传入集合类型</li>
 *   <li>LIKE操作符需要配合通配符使用</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see ConnectorEnum
 * @see com.tbs.sql.annotation.condition.QueryField
 */
public enum ContrastEnum {
    EQUAL,
    NOT_EQUAL,
    GREATER,
    LESS,
    GREATER_EQUAL,
    LESS_EQUAL,
    IS_NULL,
    IS_NOT_NULL,
    RLIKE,
    LLIKE,
    NOT_LIKE,
    IN,
    NOT_IN,
    LIKE,
    EXISTS,
    NOT_EXISTS,
    IN_SUBQUERY,
    NOT_IN_SUBQUERY
}
