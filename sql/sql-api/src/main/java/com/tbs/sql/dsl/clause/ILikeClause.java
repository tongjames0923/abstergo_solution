package com.tbs.sql.dsl.clause;

import com.tbs.common.function.SFunction;
import com.tbs.sql.dsl.IWhereClause;

/**
 * 模糊匹配子句接口，提供LIKE查询方法。
 *
 * <p>该接口定义了各种模糊匹配查询方法，包括全模糊、左模糊、右模糊和NOT LIKE，
 * 使用Lambda表达式引用实体字段，实现编译时类型检查。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>全模糊查询: {@link #like(SFunction, Object)} (LIKE '%value%')</li>
 *   <li>左模糊查询: {@link #leftLike(SFunction, Object)} (LIKE 'value%')</li>
 *   <li>右模糊查询: {@link #rightLike(SFunction, Object)} (LIKE '%value')</li>
 *   <li>不匹配查询: {@link #notLike(SFunction, Object)} (NOT LIKE)</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>构建模糊查询条件</li>
 *   <li>实现搜索功能</li>
 *   <li>构建排除条件</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * wrapper.query()
 *     .where()
 *     .like().like(User::getName, "test")
 *     .like().leftLike(User::getCode, "USER")
 *     .pour()
 *     .fetch();
 * }</pre>
 *
 * @param <T> 实体类型
 * @author abstergo
 * @since 2.0.0
 * @see IWhereClause
 */
public interface ILikeClause<T> {

    /**
     * 模糊查询（LIKE '%value%'），使用指定的比较值。
     *
     * @param acc   字段引用函数
     * @param value 比较值
     * @param <P>   字段类型
     * @return WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> like(SFunction<T, P> acc, P value);

    /**
     * 左模糊查询（LIKE 'value%'），使用指定的比较值。
     *
     * @param acc   字段引用函数
     * @param value 比较值
     * @param <P>   字段类型
     * @return WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> leftLike(SFunction<T, P> acc, P value);

    /**
     * 右模糊查询（LIKE '%value'），使用指定的比较值。
     *
     * @param acc   字段引用函数
     * @param value 比较值
     * @param <P>   字段类型
     * @return WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> rightLike(SFunction<T, P> acc, P value);

    /**
     * 不匹配查询（NOT LIKE），使用指定的比较值。
     *
     * @param acc   字段引用函数
     * @param value 比较值
     * @param <P>   字段类型
     * @return WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> notLike(SFunction<T, P> acc, P value);
}
