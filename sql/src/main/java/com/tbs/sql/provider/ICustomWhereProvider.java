package com.tbs.sql.provider;

/**
 * 根据数据主动生成where条件
 *
 * @author tongj
 */
public interface ICustomWhereProvider<T> {
    /**
     * 根据数据生成where条件
     *
     * @param value
     * @return
     */
    String getConditionSql(T value);
}
