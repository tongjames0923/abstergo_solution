package com.tbs.sql.dsl;

import com.tbs.common.function.SConsumer;
import com.tbs.common.function.SFunction;
import com.tbs.sql.enums.ConnectorEnum;
import com.tbs.sql.model.where.WhereCompose;

/**
 * WHERE子句接口，提供类型安全的查询条件构建方法。
 *
 * <p>该接口提供了丰富的查询条件构建方法，支持等值、范围、模糊、IN等多种查询条件。
 * 使用Lambda表达式引用实体字段，实现编译时类型检查。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>等值查询: {@link #equal(SFunction, Object)}</li>
 *   <li>范围查询: {@link #greaterThan}, {@link #lessThan}, {@link #greaterThanOrEqual}, {@link #lessThanOrEqual}</li>
 *   <li>模糊查询: {@link #like}, {@link #leftLike}, {@link #rightLike}</li>
 *   <li>IN查询: {@link #in(SFunction, Object)}</li>
 *   <li>空值判断: {@link #isNull}, {@link #isNotNull}</li>
 *   <li>逻辑连接: {@link #and()}, {@link #or()}</li>
 *   <li>子条件组合: {@link #andInclude}, {@link #orInclude}</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>构建动态查询条件</li>
 *   <li>实现复杂条件组合</li>
 *   <li>构建类型安全的WHERE子句</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 简单条件
 * wrapper.query()
 *     .where()
 *     .equal(User::getStatus, 1)
 *     .like(User::getName, "test")
 *     .pour()
 *     .fetch();
 * 
 * // 复杂条件组合
 * wrapper.query()
 *     .where()
 *     .like(User::getName, "test")
 *     .andInclude(where -> where
 *         .greaterThan(User::getAge, 18)
 *         .or()
 *         .equal(User::getStatus, 1)
 *     )
 *     .pour()
 *     .fetch();
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>使用 {@link #pour()} 方法返回查询构建器</li>
 *   <li>确保引用的字段在实体类中存在</li>
 *   <li>注意AND/OR的优先级</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see IQueryBuilder
 * @see IFeatureProviderPour
 */
public interface IWhereClause<T> extends IFeatureProviderPour<T> {
    /**
     * 添加AND逻辑连接符。
     *
     * @return 当前WHERE子句实例，支持链式调用
     */
    IWhereClause<T> and();

    /**
     * 添加OR逻辑连接符。
     *
     * @return 当前WHERE子句实例，支持链式调用
     */
    IWhereClause<T> or();

    /**
     * 包含子条件组合。
     *
     * @param newCondition 子条件构建器，用于构建子条件
     * @param connectorEnum 逻辑连接符类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    IWhereClause<T> include(SConsumer<IWhereClause<T>> newCondition, QueryConnectorEnum connectorEnum);

    /**
     * 使用AND连接符包含子条件组合。
     *
     * @param newCondition 子条件构建器，用于构建子条件
     * @return 当前WHERE子句实例，支持链式调用
     */
    default IWhereClause<T> andInclude(SConsumer<IWhereClause<T>> newCondition) {
        return include(newCondition, QueryConnectorEnum.AND);
    }

    /**
     * 使用OR连接符包含子条件组合。
     *
     * @param newCondition 子条件构建器，用于构建子条件
     * @return 当前WHERE子句实例，支持链式调用
     */
    default IWhereClause<T> orInclude(SConsumer<IWhereClause<T>> newCondition) {
        return include(newCondition, QueryConnectorEnum.OR);
    }

    /**
     * 判断字段是否为NULL，从实体对象获取值。
     *
     * @param acc 字段引用函数
     * @param <P> 字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> isNull(SFunction<T, P> acc);

    /**
     * 判断字段是否不为NULL，从实体对象获取值。
     *
     * @param acc 字段引用函数
     * @param <P> 字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> isNotNull(SFunction<T, P> acc);

    /**
     * 等值查询，从实体对象获取比较值。
     *
     * @param acc 字段引用函数
     * @param <P> 字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> equal(SFunction<T, P> acc);

    /**
     * 等值查询，使用指定的比较值。
     *
     * @param acc 字段引用函数
     * @param value 比较值
     * @param <P> 字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> equal(SFunction<T, P> acc, P value);

    /**
     * 小于查询，从实体对象获取比较值。
     *
     * @param acc 字段引用函数
     * @param <P> 字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> lessThan(SFunction<T, P> acc);

    /**
     * 小于查询，使用指定的比较值。
     *
     * @param acc 字段引用函数
     * @param value 比较值
     * @param <P> 字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> lessThan(SFunction<T, P> acc, P value);

    /**
     * 小于等于查询，从实体对象获取比较值。
     *
     * @param acc 字段引用函数
     * @param <P> 字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> lessThanOrEqual(SFunction<T, P> acc);

    /**
     * 小于等于查询，使用指定的比较值。
     *
     * @param acc 字段引用函数
     * @param value 比较值
     * @param <P> 字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> lessThanOrEqual(SFunction<T, P> acc, P value);

    /**
     * 大于查询，从实体对象获取比较值。
     *
     * @param acc 字段引用函数
     * @param <P> 字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> greaterThan(SFunction<T, P> acc);

    /**
     * 大于查询，使用指定的比较值。
     *
     * @param acc 字段引用函数
     * @param value 比较值
     * @param <P> 字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> greaterThan(SFunction<T, P> acc, P value);

    /**
     * 大于等于查询，从实体对象获取比较值。
     *
     * @param acc 字段引用函数
     * @param <P> 字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> greaterThanOrEqual(SFunction<T, P> acc);

    /**
     * 大于等于查询，使用指定的比较值。
     *
     * @param acc 字段引用函数
     * @param value 比较值
     * @param <P> 字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> greaterThanOrEqual(SFunction<T, P> acc, P value);

    /**
     * 不等于查询，从实体对象获取比较值。
     *
     * @param acc 字段引用函数
     * @param <P> 字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> notEqual(SFunction<T, P> acc);

    /**
     * 不等于查询，使用指定的比较值。
     *
     * @param acc 字段引用函数
     * @param value 比较值
     * @param <P> 字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> notEqual(SFunction<T, P> acc, P value);

    /**
     * 模糊查询（LIKE '%value%'），从实体对象获取比较值。
     *
     * @param acc 字段引用函数
     * @param <P> 字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> like(SFunction<T, P> acc);

    /**
     * 模糊查询（LIKE '%value%'），使用指定的比较值。
     *
     * @param acc 字段引用函数
     * @param value 比较值
     * @param <P> 字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> like(SFunction<T, P> acc, P value);

    /**
     * 左模糊查询（LIKE 'value%'），从实体对象获取比较值。
     *
     * @param acc 字段引用函数
     * @param <P> 字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> leftLike(SFunction<T, P> acc);

    /**
     * 左模糊查询（LIKE 'value%'），使用指定的比较值。
     *
     * @param acc 字段引用函数
     * @param value 比较值
     * @param <P> 字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> leftLike(SFunction<T, P> acc, P value);

    /**
     * 右模糊查询（LIKE '%value'），从实体对象获取比较值。
     *
     * @param acc 字段引用函数
     * @param <P> 字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> rightLike(SFunction<T, P> acc);

    /**
     * 右模糊查询（LIKE '%value'），使用指定的比较值。
     *
     * @param acc 字段引用函数
     * @param value 比较值
     * @param <P> 字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> rightLike(SFunction<T, P> acc, P value);

    /**
     * 不匹配查询（NOT LIKE），从实体对象获取比较值。
     *
     * @param acc 字段引用函数
     * @param <P> 字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> notLike(SFunction<T, P> acc);

    /**
     * 不匹配查询（NOT LIKE），使用指定的比较值。
     *
     * @param acc 字段引用函数
     * @param value 比较值
     * @param <P> 字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> notLike(SFunction<T, P> acc, P value);

    /**
     * IN查询，使用指定的集合值。
     *
     * @param acc 字段引用函数
     * @param values 集合值
     * @param <P> 字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> in(SFunction<T, P> acc, P values);

    /**
     * IN查询，从实体对象获取集合值。
     *
     * @param acc 字段引用函数
     * @param <P> 字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> in(SFunction<T, P> acc);

    /**
     * NOT IN查询，使用指定的集合值。
     *
     * @param acc 字段引用函数
     * @param values 集合值
     * @param <P> 字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> notIn(SFunction<T, P> acc, P values);

    /**
     * NOT IN查询，从实体对象获取集合值。
     *
     * @param acc 字段引用函数
     * @param <P> 字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    <P> IWhereClause<T> notIn(SFunction<T, P> acc);

    /**
     * 获取WHERE条件组合对象。
     *
     * @return WHERE条件组合对象
     */
    WhereCompose getCompose();

    /** 兼容旧枚举名的连接器枚举 */
    enum QueryConnectorEnum { AND, OR }
}
