package com.tbs.sql.ef;

import com.tbs.sql.framework.model.SqlLimitModel;

/**
 * 分页查询提供者
 *
 * @param <T>
 * @author abstergo
 */
public interface IQueryPageProvider<T> extends IFeatureProviderPour<T> {

    /**
     * 设置分页条件
     *
     * @param offset
     * @param count
     * @return
     */
    IQueryPageProvider<T> limit(Long offset, Long count);

    /**
     * 获取分页条件
     *
     * @param page  页码
     * @param count 每页数量
     * @return
     */
    default IQueryPageProvider<T> pageOf(Long page, Long count) {
        return limit((page - 1) * count, count);
    }

    /**
     * 获取分页条件
     *
     * @return
     */
    SqlLimitModel getLimit();

}
