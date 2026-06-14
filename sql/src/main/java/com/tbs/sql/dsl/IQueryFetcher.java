package com.tbs.sql.dsl;

import java.util.List;

/**
 * 查询结果获取接口，提供查询结果的获取方法。
 *
 * <p>该接口提供了获取查询结果的方法，支持获取列表、单个对象和总数。
 * 是查询构建器的最终执行接口。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>获取查询结果列表 {@link #fetch()}</li>
 *   <li>获取单个查询结果 {@link #fetchOne()}</li>
 *   <li>获取查询结果总数 {@link #count()}</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>执行查询并获取结果</li>
 *   <li>获取单个对象</li>
 *   <li>统计查询结果数量</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 获取结果列表
 * List<User> users = wrapper.query()
 *     .where()
 *     .equal(User::getStatus, 1)
 *     .pour()
 *     .fetch();
 * 
 * // 获取单个结果
 * User user = wrapper.query()
 *     .where()
 *     .equal(User::getId, 1L)
 *     .pour()
 *     .fetchOne();
 * 
 * // 获取总数
 * Long count = wrapper.query()
 *     .where()
 *     .equal(User::getStatus, 1)
 *     .pour()
 *     .count();
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>{@link #fetchOne()} 方法返回null如果没有找到结果</li>
 *   <li>确保查询条件正确，避免返回过多结果</li>
 *   <li>统计总数时可能会影响性能</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see IQueryBuilder
 */
public interface IQueryFetcher<T> {
    /**
     * 执行查询并返回结果列表。
     *
     * @return 查询结果列表，如果无结果则返回空列表
     */
    List<T> fetch();

    /**
     * 执行查询并返回单个结果。
     *
     * @return 单个查询结果，如果无结果则返回null
     */
    T fetchOne();

    /**
     * 执行统计查询并返回总数。
     *
     * @return 查询结果总数
     */
    Long count();
}
