package com.tbs.sql.dsl;

import com.tbs.sql.model.limit.LimitModel;

/**
 * PAGE子句接口
 *
 * @author abstergo
 * @since 2.0.0
 */
public interface IPageClause<T> extends IFeatureProviderPour<T> {
    IPageClause<T> limit(Long offset, Long count);
    default IPageClause<T> pageOf(Long page, Long count) { return limit((page - 1) * count, count); }
    LimitModel getLimit();
}
