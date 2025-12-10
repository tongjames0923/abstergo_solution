package com.tbs.sql.ef;

/**
 * 实体查询包装器接口，用于包装实体对象并提供查询功能
 * <p>
 * 该接口作为实体查询的入口点，提供了将普通实体对象转换为可查询对象的功能。
 * 通过makeQueryable方法注入实体对象，然后通过query方法获取查询构建器进行查询操作。
 * </p>
 *
 * @param <T> 实体类类型
 * @author tongj
 * @since 1.0.0
 * @see IQueryQueryBuilder
 */
public interface IEntityQueryWrapper<T> {

    /**
     * 注入分析实体使功能可用
     * <p>
     * 将指定的实体对象注入到查询包装器中，使其具备查询能力。
     * 该方法会对实体对象进行分析，提取表名、字段信息等元数据，
     * 为后续的查询操作做好准备。
     * </p>
     *
     * @param e   要注入的实体对象
     * @return 返回自身，用于链式调用
     */
    IEntityQueryWrapper<T> makeQueryable(T e);

    /**
     * 获取查询构造器
     * <p>
     * 返回查询构建器实例，用于构建和执行针对已注入实体的查询操作。
     * 在调用此方法之前需要先调用makeQueryable方法注入实体对象。
     * </p>
     *
     * @return 查询构建器实例
     * @see IQueryQueryBuilder
     */
    IQueryQueryBuilder<T> query();

}
