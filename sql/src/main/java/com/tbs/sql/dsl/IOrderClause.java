package com.tbs.sql.dsl;

import com.tbs.common.function.SFunction;
import com.tbs.sql.enums.OrderEnum;
import com.tbs.sql.model.order.OrderInfo;

import java.util.List;

/**
 * ORDER BY子句接口，提供排序条件的构建方法。
 *
 * <p>该接口提供了类型安全的排序条件构建方法，支持多字段排序，升序和降序。
 * 使用Lambda表达式引用实体字段，实现编译时类型检查。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>指定排序字段和方向 {@link #orderBy(SFunction, OrderEnum)}</li>
 *   <li>降序排序 {@link #orderByDesc(SFunction)}</li>
 *   <li>升序排序 {@link #orderByAsc(SFunction)}</li>
 *   <li>获取排序信息列表 {@link #getSqlOrders()}</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>构建排序条件</li>
 *   <li>实现多字段组合排序</li>
 *   <li>动态设置排序规则</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 单字段排序
 * wrapper.query()
 *     .where()
 *     .equal(User::getStatus, 1)
 *     .pour()
 *     .getOrderSupport().ifPresent(order -> {
 *         order.orderByDesc(User::getCreateTime);
 *     })
 *     .fetch();
 * 
 * // 多字段排序
 * wrapper.query()
 *     .where()
 *     .equal(User::getStatus, 1)
 *     .pour()
 *     .getOrderSupport().ifPresent(order -> {
 *         order.orderByDesc(User::getCreateTime)
 *              .orderByAsc(User::getName);
 *     })
 *     .fetch();
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>排序字段必须在实体类中存在</li>
 *   <li>使用 {@link #pour()} 方法返回查询构建器</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see IQueryBuilder
 * @see OrderEnum
 */
public interface IOrderClause<T> extends IFeatureProviderPour<T> {
    /**
     * 设置排序字段和方向。
     *
     * @param accessor 字段引用函数，指定排序字段
     * @param order 排序方向，ASC升序或DESC降序
     * @param <P> 字段类型
     * @return 当前排序子句实例，支持链式调用
     */
    <P> IOrderClause<T> orderBy(SFunction<T, P> accessor, OrderEnum order);

    /**
     * 设置降序排序。
     *
     * @param accessor 字段引用函数，指定排序字段
     * @param <P> 字段类型
     * @return 当前排序子句实例，支持链式调用
     */
    default <P> IOrderClause<T> orderByDesc(SFunction<T, P> accessor) { return orderBy(accessor, OrderEnum.DESC); }

    /**
     * 设置升序排序。
     *
     * @param accessor 字段引用函数，指定排序字段
     * @param <P> 字段类型
     * @return 当前排序子句实例，支持链式调用
     */
    default <P> IOrderClause<T> orderByAsc(SFunction<T, P> accessor) { return orderBy(accessor, OrderEnum.ASC); }

    /**
     * 获取排序信息列表。
     *
     * @return 排序信息列表
     */
    List<OrderInfo> getSqlOrders();
}
