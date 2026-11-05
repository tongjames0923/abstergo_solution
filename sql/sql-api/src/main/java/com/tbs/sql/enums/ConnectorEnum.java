package com.tbs.sql.enums;

/**
 * 查询连接符枚举类，定义了查询条件之间的逻辑连接方式。
 *
 * <p>该枚举类定义了SQL查询中使用的逻辑连接符，包括AND和OR两种。
 * 用于在查询条件之间建立逻辑关系。</p>
 *
 * <h3>枚举值说明</h3>
 * <ul>
 *   <li>{@link #AND}: 逻辑与连接符，对应SQL中的AND操作符</li>
 *   <li>{@link #OR}: 逻辑或连接符，对应SQL中的OR操作符</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>在查询条件之间建立逻辑关系</li>
 *   <li>在 {@link com.tbs.sql.annotation.condition.QueryField} 注解中指定连接符</li>
 *   <li>在DSL查询中使用 {@link com.tbs.sql.dsl.IWhereClause#and()} 和 {@link com.tbs.sql.dsl.IWhereClause#or()}</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 在注解中使用
 * @QueryField(connector = ConnectorEnum.AND)
 * private String field1;
 * 
 * // 在DSL中使用
 * wrapper.query()
 *     .where()
 *     .equal(User::getStatus, 1)
 *     .or()
 *     .equal(User::getStatus, 2)
 *     .pour()
 *     .fetch();
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>默认情况下使用AND连接</li>
 *   <li>注意AND和OR的优先级差异</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see ContrastEnum
 * @see com.tbs.sql.annotation.condition.QueryField
 */
public enum ConnectorEnum {
    /**
     * OR逻辑连接符枚举值
     * <p>对应SQL中的OR操作符</p>
     */
    OR,

    /**
     * AND逻辑连接符枚举值
     * <p>对应SQL中的AND操作符</p>
     */
    AND
}
