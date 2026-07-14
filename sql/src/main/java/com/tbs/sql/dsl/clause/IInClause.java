package com.tbs.sql.dsl.clause;

import com.tbs.common.function.SFunction;
import com.tbs.sql.dsl.IWhereClause;

/**
 * 集合条件子句接口，提供IN和NOT IN查询方法。
 *
 * <p>该接口定义了IN和NOT IN查询方法，用于判断字段值是否在指定集合中，
 * 使用Lambda表达式引用实体字段，实现编译时类型检查。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>IN查询: {@link #in(SFunction, Object)} (field IN (values))</li>
 *   <li>NOT IN查询: {@link #notIn(SFunction, Object)} (field NOT IN (values))</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>构建多值匹配查询条件</li>
 *   <li>构建排除条件</li>
 *   <li>批量数据筛选</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * List<Integer> statusList = Arrays.asList(1, 2, 3);
 * wrapper.query()
 *     .where()
 *     .in().in(User::getStatus, statusList)
 *     .pour()
 *     .fetch();
 * }</pre>
 *
 * @param <T> 实体类型
 * @author abstergo
 * @since 2.0.0
 * @see IWhereClause
 */
public interface IInClause<T> {

    /**
     * IN查询，使用指定的集合值。
     *
     * @param acc    字段引用函数
     * @param values 集合值
     * @param <P>    字段类型
     * @return WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> in(SFunction<T, P> acc, P values);

    /**
     * NOT IN查询，使用指定的集合值。
     *
     * @param acc    字段引用函数
     * @param values 集合值
     * @param <P>    字段类型
     * @return WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> notIn(SFunction<T, P> acc, P values);
}
