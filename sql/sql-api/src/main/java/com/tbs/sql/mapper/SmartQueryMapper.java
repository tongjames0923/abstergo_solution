package com.tbs.sql.mapper;

import com.tbs.common.util.framework.SpringUtil;
import com.tbs.sql.builder.ISqlBuildContext;
import com.tbs.sql.criteria.IPage;
import com.tbs.sql.criteria.IQueryCriteria;
import com.tbs.sql.service.IQueryEngine;

import java.util.List;

public interface SmartQueryMapper {
    /**
     * 根据查询条件、分页参数和实体类查询实体列表。
     *
     * <p>该方法构建SQL上下文和驱动，设置查询条件、分页限制和排序信息，
     * 最后通过 {@code FetchMapper} 执行查询并返回结果列表。</p>
     *
     * @param qo          查询条件对象，实现 {@link IQueryCriteria} 接口
     * @param page        分页参数，可以为null表示不分页
     * @param entityClass 实体类类型
     * @return 查询结果列表
     */
    default <T> List<T> queryByQO(IQueryCriteria qo, IPage page, Class<T> entityClass) {
        return queryByQO(null, qo, page, entityClass);
    }

    /**
     * 根据查询条件、分页参数和数据上下文查询实体列表。
     *
     * <p>该方法构建SQL上下文和驱动，设置查询条件、分页限制和排序信息，
     * 最后通过 {@code FetchMapper} 执行查询并返回结果列表。</p>
     *
     * @param sqlContext 数据上下文，实现 {@link ISqlBuildContext} 接口
     * @param qo         查询条件对象，实现 {@link IQueryCriteria} 接口
     * @param page       分页参数，可以为null表示不分页
     * @param entity     实体类类型
     */
    default <T> List<T> queryByQO(ISqlBuildContext context, IQueryCriteria qo, IPage page, Class<T> entityClass) {
        return SpringUtil.getBean(IQueryEngine.class).queryByQO(context, qo, page, entityClass);
    }


    /**
     * 根据查询条件和实体类统计符合条件的记录数。
     *
     * <p>该方法构建SQL上下文和驱动，设置查询条件，
     * 最后通过 {@code FetchMapper} 执行统计查询并返回记录数。</p>
     *
     * @param qo          查询条件对象，实现 {@link IQueryCriteria} 接口
     * @param entityClass 实体类类型
     * @return 符合条件的记录数
     */
    default <T> Integer countByQO(IQueryCriteria qo, Class<T> entityClass) {
        return countByQO(null, qo, entityClass);
    }

    /**
     * 根据查询条件和数据上下文统计符合条件的记录数。
     *
     * <p>该方法构建SQL上下文和驱动，设置查询条件，
     * 最后通过 {@code FetchMapper} 执行统计查询并返回记录数。</p>
     *
     * @param sqlContext 数据上下文，实现 {@link ISqlBuildContext} 接口
     * @param qo         查询条件对象，实现 {@link IQueryCriteria} 接口
     * @param entity     实体类类型
     * @return 符合条件的记录数
     */
    default <T> Integer countByQO(ISqlBuildContext sqlContext, IQueryCriteria qo, Class<T> entity) {
        return SpringUtil.getBean(IQueryEngine.class).countByQO(sqlContext, qo, entity);
    }
}
