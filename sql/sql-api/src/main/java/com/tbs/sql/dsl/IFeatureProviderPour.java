package com.tbs.sql.dsl;

/**
 * 功能提供者提取接口
 *
 * @author abstergo
 * @since 2.0.0
 */
public interface IFeatureProviderPour<T> {
    /**
     * 获取功能提供者
     *
     * @return
     */
    IQueryBuilder<T> pour();
}
