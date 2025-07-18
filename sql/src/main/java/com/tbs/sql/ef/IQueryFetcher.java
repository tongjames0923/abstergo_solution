package com.tbs.sql.ef;

import java.util.List;

/**
 * 查询结果接口
 *
 * @author abstergo
 * @param <T>
 */
public interface IQueryFetcher<T> {

    /**
     * 获取查询结果
     *
     * @return
     */
    List<T> fetch();

    /**
     * 获取查询第一个结果
     *
     * @return
     */
    T fetchOne();

    /**
     * 获取查询结果数量
     *
     * @return
     */
    Long count();
}
