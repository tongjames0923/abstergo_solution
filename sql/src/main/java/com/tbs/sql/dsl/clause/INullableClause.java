package com.tbs.sql.dsl.clause;

import com.tbs.common.function.SFunction;
import com.tbs.sql.dsl.IWhereClause;

/**
 * 空值判断子句接口，提供IS NULL和IS NOT NULL查询方法。
 *
 * <p>该接口定义了空值判断查询方法，用于判断字段是否为NULL或不为NULL，
 * 使用Lambda表达式引用实体字段，实现编译时类型检查。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>IS NULL查询: {@link #isNull(SFunction)} (field IS NULL)</li>
 *   <li>IS NOT NULL查询: {@link #isNotNull(SFunction)} (field IS NOT NULL)</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>筛选空值数据</li>
 *   <li>筛选非空值数据</li>
 *   <li>数据完整性检查</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * wrapper.query()
 *     .where()
 *     .nullable().isNull(User::getDeletedAt)
 *     .nullable().isNotNull(User::getEmail)
 *     .pour()
 *     .fetch();
 * }</pre>
 *
 * @param <T> 实体类型
 * @author abstergo
 * @since 2.0.0
 * @see IWhereClause
 */
public interface INullableClause<T> {

    /**
     * 判断字段是否为NULL。
     *
     * @param acc 字段引用函数
     * @param <P> 字段类型
     * @return WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> isNull(SFunction<T, P> acc);

    /**
     * 判断字段是否不为NULL。
     *
     * @param acc 字段引用函数
     * @param <P> 字段类型
     * @return WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> isNotNull(SFunction<T, P> acc);
}
