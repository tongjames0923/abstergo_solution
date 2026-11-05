package com.tbs.sql.service;

import com.tbs.sql.builder.ISqlBuildContext;
import com.tbs.sql.criteria.IPage;
import com.tbs.sql.criteria.IQueryCriteria;
import com.tbs.sql.dsl.IQueryBuilder;

import java.util.List;

/**
 * 查询引擎门面接口。
 *
 * <p>把「条件对象→SQL 构建→执行取数」的编排能力收敛到一处，由实现方（QueryEngine）在运行期
 * 提供，使 Mapper 契约接口得以仅依赖本接口而不直连引擎实现，从而支撑契约层与实现层的模块分离。</p>
 *
 * @author abstergo
 * @since 3.0.0
 */
public interface IQueryEngine {

    /**
     * 按查询条件、分页参数与实体类型查询列表。
     *
     * @param context     SQL 构建上下文，为 null 时由条件对象自动构建
     * @param qo          查询条件对象
     * @param page        分页参数，为 null 表示不分页
     * @param entityClass 实体类型
     * @param <T>         实体类型
     * @return 查询结果列表
     */
    <T> List<T> queryByQO(ISqlBuildContext context, IQueryCriteria qo, IPage page, Class<T> entityClass);

    /**
     * 按查询条件统计记录数。
     *
     * @param context SQL 构建上下文，为 null 时由条件对象自动构建
     * @param qo      查询条件对象
     * @param entity  实体类型
     * @param <T>     实体类型
     * @return 符合条件的记录数
     */
    <T> Integer countByQO(ISqlBuildContext context, IQueryCriteria qo, Class<T> entity);

    /**
     * 按实体类型获取查询构建器。
     *
     * @param entityClass 实体类型
     * @param <T>         实体类型
     * @return 查询构建器
     */
    <T> IQueryBuilder<T> query(Class<T> entityClass);

    /**
     * 按实体对象获取查询构建器。
     *
     * @param entity 实体对象
     * @param <T>    实体类型
     * @return 查询构建器
     */
    <T> IQueryBuilder<T> query(T entity);
}
