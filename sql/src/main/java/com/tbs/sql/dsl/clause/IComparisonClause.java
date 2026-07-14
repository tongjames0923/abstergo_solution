package com.tbs.sql.dsl.clause;

import com.tbs.common.function.SFunction;
import com.tbs.sql.dsl.IWhereClause;

/**
 * 比较条件子句接口，提供二元比较运算方法。
 *
 * <p>该接口定义了等值、不等于、大小比较等二元比较运算方法，
 * 使用Lambda表达式引用实体字段，实现编译时类型检查。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>等值查询: {@link #equal(SFunction, Object)}</li>
 *   <li>不等于查询: {@link #notEqual(SFunction, Object)}</li>
 *   <li>小于查询: {@link #lessThan(SFunction, Object)}</li>
 *   <li>小于等于查询: {@link #lessThanOrEqual(SFunction, Object)}</li>
 *   <li>大于查询: {@link #greaterThan(SFunction, Object)}</li>
 *   <li>大于等于查询: {@link #greaterThanOrEqual(SFunction, Object)}</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>构建等值查询条件</li>
 *   <li>构建范围查询条件</li>
 *   <li>构建不等于查询条件</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * wrapper.query()
 *     .where()
 *     .comparison().equal(User::getStatus, 1)
 *     .comparison().greaterThan(User::getAge, 18)
 *     .pour()
 *     .fetch();
 * }</pre>
 *
 * @param <T> 实体类型
 * @author abstergo
 * @since 2.0.0
 * @see IWhereClause
 */
public interface IComparisonClause<T> {

    /**
     * 等值查询，使用指定的比较值。
     *
     * @param acc   字段引用函数
     * @param value 比较值
     * @param <P>   字段类型
     * @return WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> equal(SFunction<T, P> acc, P value);

    /**
     * 不等于查询，使用指定的比较值。
     *
     * @param acc   字段引用函数
     * @param value 比较值
     * @param <P>   字段类型
     * @return WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> notEqual(SFunction<T, P> acc, P value);

    /**
     * 小于查询，使用指定的比较值。
     *
     * @param acc   字段引用函数
     * @param value 比较值
     * @param <P>   字段类型
     * @return WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> lessThan(SFunction<T, P> acc, P value);

    /**
     * 小于等于查询，使用指定的比较值。
     *
     * @param acc   字段引用函数
     * @param value 比较值
     * @param <P>   字段类型
     * @return WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> lessThanOrEqual(SFunction<T, P> acc, P value);

    /**
     * 大于查询，使用指定的比较值。
     *
     * @param acc   字段引用函数
     * @param value 比较值
     * @param <P>   字段类型
     * @return WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> greaterThan(SFunction<T, P> acc, P value);

    /**
     * 大于等于查询，使用指定的比较值。
     *
     * @param acc   字段引用函数
     * @param value 比较值
     * @param <P>   字段类型
     * @return WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> greaterThanOrEqual(SFunction<T, P> acc, P value);
}
