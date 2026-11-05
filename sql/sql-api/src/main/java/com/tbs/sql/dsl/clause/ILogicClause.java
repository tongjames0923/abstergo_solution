package com.tbs.sql.dsl.clause;

import com.tbs.sql.dsl.IWhereClause;

/**
 * 逻辑连接子句接口，提供AND和OR逻辑连接符方法。
 *
 * <p>该接口定义了AND和OR逻辑连接符方法，用于连接多个查询条件，
 * 控制条件之间的逻辑关系。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>AND连接: {@link #and()} (condition1 AND condition2)</li>
 *   <li>OR连接: {@link #or()} (condition1 OR condition2)</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>连接多个查询条件</li>
 *   <li>控制条件优先级</li>
 *   <li>构建复合查询逻辑</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * wrapper.query()
 *     .where()
 *     .equal(User::getStatus, 1)
 *     .logic().and()
 *     .equal(User::getRole, "admin")
 *     .logic().or()
 *     .equal(User::getRole, "manager")
 *     .pour()
 *     .fetch();
 * }</pre>
 *
 * @param <T> 实体类型
 * @author abstergo
 * @since 2.0.0
 * @see IWhereClause
 */
public interface ILogicClause<T> {

    /**
     * 添加AND逻辑连接符。
     *
     * @return WHERE子句实例，支持链式调用
     */
    IWhereClause<T> and();

    /**
     * 添加OR逻辑连接符。
     *
     * @return WHERE子句实例，支持链式调用
     */
    IWhereClause<T> or();
}
