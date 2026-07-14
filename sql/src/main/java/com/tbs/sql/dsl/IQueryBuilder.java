package com.tbs.sql.dsl;

import com.tbs.common.function.SBiConsumer;
import com.tbs.common.function.SConsumer;
import com.tbs.common.function.SFunction;
import com.tbs.sql.builder.SqlWithParams;

import java.util.Optional;

/**
 * 查询构建器接口，提供查询的构建和执行方法。
 *
 * <p>该接口是查询构建的核心接口，提供了设置实体、构建条件、执行查询等方法。
 * 支持链式调用，可以构建复杂的查询语句。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>获取实体对象 {@link #getEntity()}</li>
 *   <li>设置实体属性 {@link #access(SBiConsumer, Object)}</li>
 *   <li>执行操作 {@link #op(SConsumer)}</li>
 *   <li>构建WHERE条件 {@link #where()}</li>
 *   <li>获取排序支持 {@link #getOrderSupport()}</li>
 *   <li>获取分页支持 {@link #getPageLimitSupport()}</li>
 *   <li>设置分组 {@link #groupBy(SFunction)}</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>构建和执行查询</li>
 *   <li>设置查询条件</li>
 *   <li>构建复杂的查询语句</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 构建查询
 * List<User> users = wrapper.query()
 *     .where()
 *     .equal(User::getStatus, 1)
 *     .pour()
 *     .fetch();
 * 
 * // 构建复杂查询
 * List<User> users = wrapper.query()
 *     .where()
 *     .like(User::getName, "test")
 *     .andInclude(where -> where
 *         .greaterThan(User::getAge, 18)
 *     )
 *     .pour()
 *     .getOrderSupport()
 *     .map(order -> order.orderByDesc(User::getCreateTime))
 *     .orElse(null)
 *     .getPageLimitSupport()
 *     .map(page -> page.pageOf(1L, 10L))
 *     .orElse(null)
 *     .fetch();
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>使用 {@link #where()} 方法开始构建WHERE条件</li>
 *   <li>使用 {@link #fetch()} 方法执行查询</li>
 *   <li>确保实体类正确配置了表名和字段映射</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see IWhereClause
 * @see IOrderClause
 * @see IPageClause
 * @see IQueryFetcher
 */
public interface IQueryBuilder<T> extends IQueryFetcher<T> {
    /**
     * 获取当前实体对象。
     *
     * @return 实体对象
     */
    T getEntity();

    /**
     * 设置实体属性值。
     *
     * @param accessor 属性设置器，用于设置实体属性
     * @param property 属性值
     * @param <P> 属性类型
     * @return 当前查询构建器实例，支持链式调用
     */
    <P> IQueryBuilder<T> access(SBiConsumer<T, P> accessor, P property);

    /**
     * 对实体执行自定义操作。
     *
     * @param operator 操作器，用于执行自定义操作
     * @return 当前查询构建器实例，支持链式调用
     */
    IQueryBuilder<T> op(SConsumer<T> operator);

    /**
     * 获取WHERE子句构建器。
     *
     * @return WHERE子句构建器实例
     */
    IWhereClause<T> where();

    /**
     * 获取排序支持。
     *
     * @return 排序子句的Optional包装，如果不支持排序则为空
     */
    Optional<IOrderClause<T>> getOrderSupport();

    /**
     * 获取分页支持。
     *
     * @return 分页子句的Optional包装，如果不支持分页则为空
     */
    Optional<IPageClause<T>> getPageLimitSupport();

    /**
     * 设置分组字段。
     *
     * @param accessor 字段引用函数，指定分组字段
     * @param <P> 字段类型
     * @return 当前查询构建器实例，支持链式调用
     */
    <P> IQueryBuilder<T> groupBy(SFunction<T, P> accessor);

    /**
     * 将当前查询构建器转换为SQL字符串（用于子查询）。
     *
     * <p>该方法将当前查询条件生成为SELECT SQL字符串，可用于子查询场景。
     * 生成的SQL不包含分页和排序信息，仅包含SELECT和WHERE部分。</p>
     *
     * @return SQL字符串
     */
    String toSql();

    /**
     * 将当前查询构建器转换为SQL字符串及其参数（用于子查询）。
     *
     * <p>该方法将当前查询条件生成为SELECT SQL字符串及其参数映射，
     * 可用于子查询场景，支持参数传递到父查询。</p>
     *
     * @return SQL语句及其参数的封装对象
     */
    SqlWithParams toSqlWithParams();
}
