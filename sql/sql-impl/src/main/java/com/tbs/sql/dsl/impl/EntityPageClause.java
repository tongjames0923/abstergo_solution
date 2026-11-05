package com.tbs.sql.dsl.impl;

import com.tbs.sql.dsl.IPageClause;
import com.tbs.sql.dsl.IQueryBuilder;
import com.tbs.sql.model.limit.LimitModel;

public class EntityPageClause<T> implements IPageClause<T> {
    private IQueryBuilder<T> queryBuilder;
    private Long offset, count;

    public EntityPageClause(IQueryBuilder<T> queryBuilder) { this.queryBuilder = queryBuilder; }

    @Override public IPageClause<T> limit(Long offset, Long count) { this.offset = offset; this.count = count; return this; }
    @Override public LimitModel getLimit() { return new LimitModel(offset, count); }
    @Override public IQueryBuilder<T> pour() { return queryBuilder; }
}
