package com.tbs.sql.dsl;

import com.tbs.common.function.SConsumer;
import com.tbs.common.function.SFunction;
import com.tbs.sql.dsl.clause.*;
import com.tbs.sql.enums.ConnectorEnum;
import com.tbs.sql.model.where.WhereCompose;

/**
 * WHERE子句接口，提供类型安全的查询条件构建方法。
 *
 * <p>该接口采用组合模式设计，通过聚合多个子句接口提供丰富的查询条件构建方法。
 * 支持等值、范围、模糊、IN等多种查询条件，使用Lambda表达式引用实体字段，
 * 实现编译时类型检查。</p>
 *
 * <h3>设计模式</h3>
 * <p>采用组合模式，将查询条件按语义分类为独立的子句接口：</p>
 * <ul>
 *   <li>{@link IComparisonClause} - 比较条件（等值、不等于、大小比较）</li>
 *   <li>{@link ILikeClause} - 模糊匹配条件</li>
 *   <li>{@link IInClause} - 集合条件（IN、NOT IN）</li>
 *   <li>{@link INullableClause} - 空值判断条件</li>
 *   <li>{@link ISubConditionClause} - 子条件组合</li>
 *   <li>{@link ISubQueryClause} - 子查询条件（EXISTS、IN子查询）</li>
 *   <li>{@link ILogicClause} - 逻辑连接符</li>
 * </ul>
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
 * // 简单条件（使用默认方法，扁平调用）
 * wrapper.query()
 *     .where()
 *     .equal(User::getStatus, 1)
 *     .like(User::getName, "test")
 *     .pour()
 *     .fetch();
 *
 * // 显式获取子句调用
 * wrapper.query()
 *     .where()
 *     .comparison().equal(User::getStatus, 1)
 *     .like().like(User::getName, "test")
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
 * @param <T> 实体类型
 * @author abstergo
 * @since 2.0.0
 * @see IQueryBuilder
 * @see IFeatureProviderPour
 * @see IComparisonClause
 * @see ILikeClause
 * @see IInClause
 * @see INullableClause
 * @see ISubConditionClause
 * @see ISubQueryClause
 * @see ILogicClause
 */
public interface IWhereClause<T> extends IFeatureProviderPour<T> {

    // ============ 子句访问方法（组合模式）============

    /**
     * 获取比较条件子句。
     *
     * @return 比较条件子句实例
     */
    IComparisonClause<T> comparison();

    /**
     * 获取模糊匹配子句。
     *
     * @return 模糊匹配子句实例
     */
    ILikeClause<T> like();

    /**
     * 获取集合条件子句。
     *
     * @return 集合条件子句实例
     */
    IInClause<T> in();

    /**
     * 获取空值判断子句。
     *
     * @return 空值判断子句实例
     */
    INullableClause<T> nullable();

    /**
     * 获取子条件组合子句。
     *
     * @return 子条件组合子句实例
     */
    ISubConditionClause<T> sub();

    /**
     * 获取逻辑连接子句。
     *
     * @return 逻辑连接子句实例
     */
    ILogicClause<T> logic();

    /**
     * 获取子查询条件子句。
     *
     * @return 子查询条件子句实例
     */
    ISubQueryClause<T> subQuery();

    // ============ 核心方法 ============

    /**
     * 获取WHERE条件组合对象。
     *
     * @return WHERE条件组合对象
     */
    WhereCompose getCompose();

    // ============ 默认方法（保持扁平调用体验）============

    /**
     * 添加AND逻辑连接符。
     *
     * @return 当前WHERE子句实例，支持链式调用
     */
    default IWhereClause<T> and() {
        return logic().and();
    }

    /**
     * 添加OR逻辑连接符。
     *
     * @return 当前WHERE子句实例，支持链式调用
     */
    default IWhereClause<T> or() {
        return logic().or();
    }

    /**
     * 包含子条件组合。
     *
     * @param newCondition  子条件构建器，用于构建子条件
     * @param connectorEnum 逻辑连接符类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    default IWhereClause<T> include(SConsumer<IWhereClause<T>> newCondition, QueryConnectorEnum connectorEnum) {
        return sub().include(newCondition, connectorEnum);
    }

    /**
     * 使用AND连接符包含子条件组合。
     *
     * @param newCondition 子条件构建器，用于构建子条件
     * @return 当前WHERE子句实例，支持链式调用
     */
    default IWhereClause<T> andInclude(SConsumer<IWhereClause<T>> newCondition) {
        return sub().andInclude(newCondition);
    }

    /**
     * 使用OR连接符包含子条件组合。
     *
     * @param newCondition 子条件构建器，用于构建子条件
     * @return 当前WHERE子句实例，支持链式调用
     */
    default IWhereClause<T> orInclude(SConsumer<IWhereClause<T>> newCondition) {
        return sub().orInclude(newCondition);
    }

    /**
     * 判断字段是否为NULL。
     *
     * @param acc 字段引用函数
     * @param <P> 字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    default <P> IWhereClause<T> isNull(SFunction<T, P> acc) {
        return nullable().isNull(acc);
    }

    /**
     * 判断字段是否不为NULL。
     *
     * @param acc 字段引用函数
     * @param <P> 字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    default <P> IWhereClause<T> isNotNull(SFunction<T, P> acc) {
        return nullable().isNotNull(acc);
    }

    /**
     * 等值查询，使用指定的比较值。
     *
     * @param acc   字段引用函数
     * @param value 比较值
     * @param <P>   字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    default <P> IWhereClause<T> equal(SFunction<T, P> acc, P value) {
        return comparison().equal(acc, value);
    }

    /**
     * 小于查询，使用指定的比较值。
     *
     * @param acc   字段引用函数
     * @param value 比较值
     * @param <P>   字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    default <P> IWhereClause<T> lessThan(SFunction<T, P> acc, P value) {
        return comparison().lessThan(acc, value);
    }

    /**
     * 小于等于查询，使用指定的比较值。
     *
     * @param acc   字段引用函数
     * @param value 比较值
     * @param <P>   字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    default <P> IWhereClause<T> lessThanOrEqual(SFunction<T, P> acc, P value) {
        return comparison().lessThanOrEqual(acc, value);
    }

    /**
     * 大于查询，使用指定的比较值。
     *
     * @param acc   字段引用函数
     * @param value 比较值
     * @param <P>   字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    default <P> IWhereClause<T> greaterThan(SFunction<T, P> acc, P value) {
        return comparison().greaterThan(acc, value);
    }

    /**
     * 大于等于查询，使用指定的比较值。
     *
     * @param acc   字段引用函数
     * @param value 比较值
     * @param <P>   字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    default <P> IWhereClause<T> greaterThanOrEqual(SFunction<T, P> acc, P value) {
        return comparison().greaterThanOrEqual(acc, value);
    }

    /**
     * 不等于查询，使用指定的比较值。
     *
     * @param acc   字段引用函数
     * @param value 比较值
     * @param <P>   字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    default <P> IWhereClause<T> notEqual(SFunction<T, P> acc, P value) {
        return comparison().notEqual(acc, value);
    }

    /**
     * 模糊查询（LIKE '%value%'），使用指定的比较值。
     *
     * @param acc   字段引用函数
     * @param value 比较值
     * @param <P>   字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    default <P> IWhereClause<T> like(SFunction<T, P> acc, P value) {
        return like().like(acc, value);
    }

    /**
     * 左模糊查询（LIKE 'value%'），使用指定的比较值。
     *
     * @param acc   字段引用函数
     * @param value 比较值
     * @param <P>   字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    default <P> IWhereClause<T> leftLike(SFunction<T, P> acc, P value) {
        return like().leftLike(acc, value);
    }

    /**
     * 右模糊查询（LIKE '%value'），使用指定的比较值。
     *
     * @param acc   字段引用函数
     * @param value 比较值
     * @param <P>   字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    default <P> IWhereClause<T> rightLike(SFunction<T, P> acc, P value) {
        return like().rightLike(acc, value);
    }

    /**
     * 不匹配查询（NOT LIKE），使用指定的比较值。
     *
     * @param acc   字段引用函数
     * @param value 比较值
     * @param <P>   字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    default <P> IWhereClause<T> notLike(SFunction<T, P> acc, P value) {
        return like().notLike(acc, value);
    }

    /**
     * IN查询，使用指定的集合值。
     *
     * @param acc    字段引用函数
     * @param values 集合值
     * @param <P>    字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    default <P> IWhereClause<T> in(SFunction<T, P> acc, P values) {
        return in().in(acc, values);
    }

    /**
     * NOT IN查询，使用指定的集合值。
     *
     * @param acc    字段引用函数
     * @param values 集合值
     * @param <P>    字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    default <P> IWhereClause<T> notIn(SFunction<T, P> acc, P values) {
        return in().notIn(acc, values);
    }

    // ============ 子查询默认方法 ============

    /**
     * EXISTS子查询，判断子查询是否返回结果。
     *
     * @param subQuery 子查询构建器
     * @return 当前WHERE子句实例，支持链式调用
     */
    default IWhereClause<T> exists(IQueryBuilder<?> subQuery) {
        return subQuery().exists(subQuery);
    }

    /**
     * NOT EXISTS子查询，判断子查询是否不返回结果。
     *
     * @param subQuery 子查询构建器
     * @return 当前WHERE子句实例，支持链式调用
     */
    default IWhereClause<T> notExists(IQueryBuilder<?> subQuery) {
        return subQuery().notExists(subQuery);
    }

    /**
     * IN子查询，判断字段值是否在子查询结果中。
     *
     * @param acc      字段引用函数
     * @param subQuery 子查询构建器
     * @param <P>      字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    default <P> IWhereClause<T> inSubQuery(SFunction<T, P> acc, IQueryBuilder<?> subQuery) {
        return subQuery().in(acc, subQuery);
    }

    /**
     * NOT IN子查询，判断字段值是否不在子查询结果中。
     *
     * @param acc      字段引用函数
     * @param subQuery 子查询构建器
     * @param <P>      字段类型
     * @return 当前WHERE子句实例，支持链式调用
     */
    default <P> IWhereClause<T> notInSubQuery(SFunction<T, P> acc, IQueryBuilder<?> subQuery) {
        return subQuery().notIn(acc, subQuery);
    }

    // ============ 兼容旧枚举名的连接器枚举 ============

    /**
     * 连接器枚举，用于子条件组合。
     */
    enum QueryConnectorEnum {
        AND, OR
    }
}
