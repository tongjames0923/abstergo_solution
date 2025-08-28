package com.tbs.sql.ef;

/**
 * 功能提供者提取接口，用于提取查询构建器
 * <p>
 * 该接口作为各种功能提供者接口的基接口，定义了提取查询构建器的方法。
 * 通过pour()方法可以获取到查询构建器，从而继续构建完整的查询语句。
 * </p>
 *
 * @param <T> 实体类类型
 * @author abstergo
 * @see IQueryQueryBuilder
 */
public interface IFeatureProviderPour<T> {
    /**
     * 提取查询构建器
     * <p>
     * 从当前功能提供者中提取查询构建器，用于继续构建查询语句。
     * 该方法是连接各个功能提供者与查询构建器的桥梁，实现了功能模块的解耦。
     * </p>
     *
     * @return 查询构建器实例
     * @see IQueryQueryBuilder
     */
    IQueryQueryBuilder<T> pour();
}
