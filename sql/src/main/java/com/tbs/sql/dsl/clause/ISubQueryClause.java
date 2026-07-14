package com.tbs.sql.dsl.clause;

import com.tbs.common.function.SFunction;
import com.tbs.sql.dsl.IQueryBuilder;
import com.tbs.sql.dsl.IWhereClause;

/**
 * 子查询条件子句接口，提供跨实体的子查询条件方法。
 *
 * <p>该接口定义了子查询条件方法，支持EXISTS、NOT EXISTS、IN子查询、NOT IN子查询等
 * 跨实体查询条件，用于构建复杂的关联查询。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>EXISTS子查询: {@link #exists(IQueryBuilder)} (EXISTS (SELECT ...))</li>
 *   <li>NOT EXISTS子查询: {@link #notExists(IQueryBuilder)} (NOT EXISTS (SELECT ...))</li>
 *   <li>IN子查询: {@link #in(SFunction, IQueryBuilder)} (field IN (SELECT ...))</li>
 *   <li>NOT IN子查询: {@link #notIn(SFunction, IQueryBuilder)} (field NOT IN (SELECT ...))</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>查询存在关联数据的记录</li>
 *   <li>查询不存在关联数据的记录</li>
 *   <li>基于子查询结果过滤数据</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // EXISTS 子查询：查询有订单的用户
 * IQueryBuilder<Order> orderQuery = wrapper.query(Order.class)
 *     .where()
 *     .equal(Order::getUserId, /* 关联字段 * /)
 *     .pour();
 *
 * wrapper.query(User.class)
 *     .where()
 *     .subQuery().exists(orderQuery)
 *     .pour()
 *     .fetch();
 *
 * // IN 子查询：查询在指定部门的用户
 * IQueryBuilder<Department> deptQuery = wrapper.query(Department.class)
 *     .where()
 *     .equal(Department::getCompanyId, companyId)
 *     .pour();
 *
 * wrapper.query(User.class)
 *     .where()
 *     .subQuery().in(User::getDeptId, deptQuery)
 *     .pour()
 *     .fetch();
 * }</pre>
 *
 * @param <T> 实体类型
 * @author abstergo
 * @since 2.0.0
 * @see IWhereClause
 * @see IQueryBuilder
 */
public interface ISubQueryClause<T> {

    /**
     * EXISTS子查询，判断子查询是否返回结果。
     *
     * <p>生成SQL: EXISTS (SELECT ... FROM ... WHERE ...)</p>
     *
     * @param subQuery 子查询构建器
     * @return WHERE子句实例，支持链式调用
     */
    IWhereClause<T> exists(IQueryBuilder<?> subQuery);

    /**
     * NOT EXISTS子查询，判断子查询是否不返回结果。
     *
     * <p>生成SQL: NOT EXISTS (SELECT ... FROM ... WHERE ...)</p>
     *
     * @param subQuery 子查询构建器
     * @return WHERE子句实例，支持链式调用
     */
    IWhereClause<T> notExists(IQueryBuilder<?> subQuery);

    /**
     * IN子查询，判断字段值是否在子查询结果中。
     *
     * <p>生成SQL: field IN (SELECT ... FROM ... WHERE ...)</p>
     *
     * @param acc      字段引用函数
     * @param subQuery 子查询构建器
     * @param <P>      字段类型
     * @return WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> in(SFunction<T, P> acc, IQueryBuilder<?> subQuery);

    /**
     * NOT IN子查询，判断字段值是否不在子查询结果中。
     *
     * <p>生成SQL: field NOT IN (SELECT ... FROM ... WHERE ...)</p>
     *
     * @param acc      字段引用函数
     * @param subQuery 子查询构建器
     * @param <P>      字段类型
     * @return WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> notIn(SFunction<T, P> acc, IQueryBuilder<?> subQuery);
}
