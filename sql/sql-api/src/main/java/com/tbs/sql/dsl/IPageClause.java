package com.tbs.sql.dsl;

import com.tbs.sql.model.limit.LimitModel;

/**
 * 分页子句接口，提供分页查询的构建方法。
 *
 * <p>该接口提供了分页查询的构建方法，支持偏移量和数据量的设置，以及页码式的分页。
 * 可以获取分页模型供SQL引擎使用。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>设置偏移量和数据量 {@link #limit(Long, Long)}</li>
 *   <li>页码式分页 {@link #pageOf(Long, Long)}</li>
 *   <li>获取分页模型 {@link #getLimit()}</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>实现分页查询</li>
 *   <li>控制查询结果的数量</li>
 *   <li>实现大数据量的分页显示</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 使用偏移量和数据量
 * wrapper.query()
 *     .where()
 *     .equal(User::getStatus, 1)
 *     .pour()
 *     .getPageLimitSupport().ifPresent(page -> {
 *         page.limit(0L, 10L);  // 从第0条开始，取10条
 *     })
 *     .fetch();
 * 
 * // 使用页码式分页
 * wrapper.query()
 *     .where()
 *     .equal(User::getStatus, 1)
 *     .pour()
 *     .getPageLimitSupport().ifPresent(page -> {
 *         page.pageOf(1L, 10L);  // 第1页，每页10条
 *     })
 *     .fetch();
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>偏移量从0开始</li>
 *   <li>数据量应该大于0</li>
 *   <li>页码从1开始</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see IQueryBuilder
 * @see LimitModel
 */
public interface IPageClause<T> extends IFeatureProviderPour<T> {
    /**
     * 设置分页偏移量和数据量。
     *
     * @param offset 偏移量，从0开始
     * @param count 数据量，每页显示的记录数
     * @return 当前分页子句实例，支持链式调用
     */
    IPageClause<T> limit(Long offset, Long count);

    /**
     * 使用页码式分页。
     *
     * @param page 页码，从1开始
     * @param count 每页显示的记录数
     * @return 当前分页子句实例，支持链式调用
     */
    default IPageClause<T> pageOf(Long page, Long count) { return limit((page - 1) * count, count); }

    /**
     * 获取分页模型。
     *
     * @return 分页模型对象
     */
    LimitModel getLimit();
}
