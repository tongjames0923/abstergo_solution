package com.tbs.sql.ef.impl;

import com.tbs.sql.ef.IFeatureProviderPour;
import com.tbs.sql.ef.IQueryQueryBuilder;

/**
 * 基础提取器
 *
 * @author abstergo
 */
public class SimpleFeatureProviderPour<T> implements IFeatureProviderPour<T> {

    private IQueryQueryBuilder<T> queryBuilder;

    public SimpleFeatureProviderPour(IQueryQueryBuilder<T> queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    @Override
    public IQueryQueryBuilder<T> pour() {
        return queryBuilder;
    }
}
