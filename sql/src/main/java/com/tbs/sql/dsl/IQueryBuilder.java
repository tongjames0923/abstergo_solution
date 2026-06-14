package com.tbs.sql.dsl;

import com.tbs.common.function.SBiConsumer;
import com.tbs.common.function.SConsumer;
import com.tbs.common.function.SFunction;

import java.util.Optional;

/**
 * 查询构建器接口
 *
 * @author abstergo
 * @since 2.0.0
 */
public interface IQueryBuilder<T> extends IQueryFetcher<T> {
    T getEntity();
    <P> IQueryBuilder<T> access(SBiConsumer<T, P> accessor, P property);
    IQueryBuilder<T> op(SConsumer<T> operator);
    IWhereClause<T> where();
    Optional<IOrderClause<T>> getOrderSupport();
    Optional<IPageClause<T>> getPageLimitSupport();
    <P> IQueryBuilder<T> groupBy(SFunction<T, P> accessor);
}
