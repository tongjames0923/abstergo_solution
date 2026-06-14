package com.tbs.sql.dsl;

import com.tbs.common.function.SFunction;
import com.tbs.sql.enums.OrderEnum;
import com.tbs.sql.model.order.OrderInfo;

import java.util.List;

/**
 * ORDER子句接口
 *
 * @author abstergo
 * @since 2.0.0
 */
public interface IOrderClause<T> extends IFeatureProviderPour<T> {
    <P> IOrderClause<T> orderBy(SFunction<T, P> accessor, OrderEnum order);
    default <P> IOrderClause<T> orderByDesc(SFunction<T, P> accessor) { return orderBy(accessor, OrderEnum.DESC); }
    default <P> IOrderClause<T> orderByAsc(SFunction<T, P> accessor) { return orderBy(accessor, OrderEnum.ASC); }
    List<OrderInfo> getSqlOrders();
}
