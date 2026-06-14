package com.tbs.sql.dsl;

/**
 * 实体查询包装器接口
 *
 * @author abstergo
 * @since 2.0.0
 */
public interface IEntityQueryWrapper<T> {
    IEntityQueryWrapper<T> makeQueryable(T e);
    IQueryBuilder<T> query();
}
