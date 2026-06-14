package com.tbs.sql.dsl;

import java.util.List;

/**
 * 查询结果获取接口
 *
 * @author abstergo
 * @since 2.0.0
 */
public interface IQueryFetcher<T> {
    List<T> fetch();
    T fetchOne();
    Long count();
}
