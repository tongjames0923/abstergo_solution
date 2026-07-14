package com.tbs.sql.dsl.clause;

import com.tbs.common.function.SConsumer;
import com.tbs.sql.dsl.IWhereClause;
import com.tbs.sql.dsl.IWhereClause.QueryConnectorEnum;

/**
 * 子条件组合子句接口，提供子条件组合方法。
 *
 * <p>该接口定义了子条件组合方法，用于构建复杂的嵌套条件表达式，
 * 支持AND和OR两种连接方式。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>AND子条件: {@link #andInclude(SConsumer)} ((condition1 AND condition2))</li>
 *   <li>OR子条件: {@link #orInclude(SConsumer)} ((condition1 OR condition2))</li>
 *   <li>自定义连接: {@link #include(SConsumer, QueryConnectorEnum)}</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>构建复杂嵌套条件</li>
 *   <li>实现优先级分组</li>
 *   <li>构建多条件组合查询</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * wrapper.query()
 *     .where()
 *     .equal(User::getStatus, 1)
 *     .sub().andInclude(w -> w
 *         .greaterThan(User::getAge, 18)
 *         .or()
 *         .equal(User::getRole, "admin")
 *     )
 *     .pour()
 *     .fetch();
 * }</pre>
 *
 * @param <T> 实体类型
 * @author abstergo
 * @see IWhereClause
 * @since 2.0.0
 */
public interface ISubConditionClause<T> {

    /**
     * 包含子条件组合。
     *
     * @param newCondition  子条件构建器，用于构建子条件
     * @param connectorEnum 逻辑连接符类型
     * @return WHERE子句实例，支持链式调用
     */
    IWhereClause<T> include(SConsumer<IWhereClause<T>> newCondition, QueryConnectorEnum connectorEnum);

    /**
     * 使用AND连接符包含子条件组合。
     *
     * @param newCondition 子条件构建器，用于构建子条件
     * @return WHERE子句实例，支持链式调用
     */
    default IWhereClause<T> andInclude(SConsumer<IWhereClause<T>> newCondition) {
        return include(newCondition, QueryConnectorEnum.AND);
    }

    /**
     * 使用OR连接符包含子条件组合。
     *
     * @param newCondition 子条件构建器，用于构建子条件
     * @return WHERE子句实例，支持链式调用
     */
    default IWhereClause<T> orInclude(SConsumer<IWhereClause<T>> newCondition) {
        return include(newCondition, QueryConnectorEnum.OR);
    }
}
