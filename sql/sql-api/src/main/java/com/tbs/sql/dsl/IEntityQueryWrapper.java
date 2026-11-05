package com.tbs.sql.dsl;

/**
 * 实体查询包装器接口，提供实体查询的入口。
 *
 * <p>该接口是TBS SQL框架的核心查询入口，提供了创建实体查询包装器和查询构建器的方法。
 * 通过此接口可以开始构建类型安全的查询语句。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>创建可查询的实体包装器 {@link #makeQueryable(Object)}</li>
 *   <li>获取查询构建器 {@link #query()}</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>开始构建实体查询</li>
 *   <li>创建类型安全的查询语句</li>
 *   <li>使用DSL风格构建查询</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 创建实体查询包装器
 * IEntityQueryWrapper<User> wrapper = QueryEngine.getEntityQueryWrapper(User.class);
 * 
 * // 开始构建查询
 * List<User> users = wrapper.query()
 *     .where()
 *     .equal(User::getStatus, 1)
 *     .like(User::getName, "test")
 *     .pour()
 *     .fetch();
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>每次调用 {@link #query()} 都会创建新的查询构建器</li>
 *   <li>确保实体类正确配置了表名和字段映射</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see IQueryBuilder
 * @see com.tbs.sql.service.QueryEngine
 */
public interface IEntityQueryWrapper<T> {
    /**
     * 设置可查询的实体对象。
     *
     * @param e 实体对象，不能为空
     * @return 当前包装器实例，支持链式调用
     */
    IEntityQueryWrapper<T> makeQueryable(T e);

    /**
     * 获取查询构建器，用于构建查询条件。
     *
     * @return 查询构建器实例
     */
    IQueryBuilder<T> query();
}
