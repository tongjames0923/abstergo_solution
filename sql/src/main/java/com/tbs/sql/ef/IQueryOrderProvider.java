package com.tbs.sql.ef;


import com.tbs.common.function.SFunction;
import com.tbs.sql.enums.QueryOrderEnum;
import com.tbs.sql.model.SqlOrderInfo;

import java.util.List;

/**
 * 排序提供者
 *
 * @author abstergo
 */
public interface IQueryOrderProvider<T> extends IFeatureProviderPour<T> {
    /**
     * 排序
     *
     * @param accessor
     * @param order
     * @param <P>
     * @return
     */
    <P> IQueryOrderProvider<T> orderBy(SFunction<T, P> accessor, QueryOrderEnum order);

    /**
     * 倒序排序
     *
     * @param accessor
     * @param <P>
     * @return
     */
    default <P> IQueryOrderProvider<T> orderByDesc(SFunction<T, P> accessor) {
        return orderBy(accessor, QueryOrderEnum.DESC);
    }

    /**
     * 正序排序
     *
     * @param accessor
     * @param <P>
     * @return
     */
    default <P> IQueryOrderProvider<T> orderByAsc(SFunction<T, P> accessor) {
        return orderBy(accessor, QueryOrderEnum.ASC);
    }

    /**
     * 获取排序信息
     *
     * @return
     */
    List<SqlOrderInfo> getSqlOrders();

}
