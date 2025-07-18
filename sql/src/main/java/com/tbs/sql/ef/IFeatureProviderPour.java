package com.tbs.sql.ef;

/**
 * 提取器
 *
 * @param <T>
 * @author abstergo
 */
public interface IFeatureProviderPour<T> {
    /**
     * 提取
     *
     * @return
     */
    IQueryQueryBuilder<T> pour();
}
