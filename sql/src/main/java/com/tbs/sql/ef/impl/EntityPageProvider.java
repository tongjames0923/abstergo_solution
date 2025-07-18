package com.tbs.sql.ef.impl;

import com.tbs.sql.ef.IQueryPageProvider;
import com.tbs.sql.ef.IQueryQueryBuilder;
import com.tbs.sql.model.SqlLimitModel;

/**
 * 分页提供者
 *
 * @author abstergo
 */
public class EntityPageProvider<T> implements IQueryPageProvider<T> {

    private IQueryQueryBuilder<T> queryBuilder;

    private Long offset, count;

    public EntityPageProvider(IQueryQueryBuilder<T> queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    @Override
    public IQueryPageProvider<T> limit(Long offset, Long count) {
        this.offset = offset;
        this.count = count;
        return this;
    }

    @Override
    public SqlLimitModel getLimit() {
        return new SqlLimitModel(offset, count);
    }

    @Override
    public IQueryQueryBuilder<T> pour() {
        return queryBuilder;
    }
}
