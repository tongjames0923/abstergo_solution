package com.tbs.sql.ef;

import com.tbs.sql.framework.model.SqlLimitModel;

/**
 * 分页查询提供者接口，用于提供SQL查询中的分页功能
 * <p>
 * 该接口提供了分页查询的相关方法，支持设置偏移量和数量来实现分页，
 * 以及通过页码和每页数量来实现分页的功能。
 * </p>
 *
 * @param <T> 实体类类型
 * @author abstergo
 * @see SqlLimitModel
 * @see IFeatureProviderPour
 */
public interface IQueryPageProvider<T> extends IFeatureProviderPour<T> {

    /**
     * 设置分页条件
     * <p>
     * 通过指定偏移量和记录数来设置分页条件，生成SQL中的LIMIT offset, count子句。
     * 例如: LIMIT 10, 20 表示从第11条记录开始，取20条记录。
     * </p>
     *
     * @param offset 偏移量，从0开始计数，表示跳过前面多少条记录
     * @param count  记录数，表示要获取的记录数量
     * @return 返回自身，用于链式调用
     */
    IQueryPageProvider<T> limit(Long offset, Long count);

    /**
     * 获取分页条件
     * <p>
     * 通过页码和每页数量来设置分页条件，是limit方法的便捷调用方式。
     * 页码从1开始计数，内部会自动转换为偏移量(offset = (page - 1) * count)。
     * 例如: pageOf(2, 10) 表示获取第2页，每页10条记录的数据。
     * </p>
     *
     * @param page  页码，从1开始计数
     * @param count 每页数量，表示每页要获取的记录数量
     * @return 返回自身，用于链式调用
     */
    default IQueryPageProvider<T> pageOf(Long page, Long count) {
        return limit((page - 1) * count, count);
    }

    /**
     * 获取分页条件
     * <p>
     * 返回封装了分页信息的模型对象，用于后续SQL语句中LIMIT子句的生成。
     * </p>
     *
     * @return 分页条件模型对象
     * @see SqlLimitModel
     */
    SqlLimitModel getLimit();

}
