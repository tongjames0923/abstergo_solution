package com.tbs.sql.provider;

/**
 * 自定义WHERE条件提供者接口
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface ICustomWhereProvider<T> {
    String getConditionSql(T value);
}
