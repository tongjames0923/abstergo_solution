package com.tbs.sql.ef;

/**
 * @author tongj
 */
public interface IEntityQueryWrapper<T> {

    /**
     * 注入分析实体使功能可用
     *
     * @param e
     * @return
     */
    IEntityQueryWrapper<T> makeQueryable(T e);

    /**
     * 获取查询构造器
     *
     * @return
     */
    IQueryQueryBuilder<T> query();

}
