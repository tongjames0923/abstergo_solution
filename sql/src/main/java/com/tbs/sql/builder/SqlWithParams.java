package com.tbs.sql.builder;

import java.util.Map;

/**
 * SQL语句及其参数的封装类。
 *
 * <p>该类用于封装生成的SQL语句及其对应的参数映射，
 * 主要用于子查询场景，需要将子查询的SQL和参数传递给父查询。</p>
 *
 * @author abstergo
 * @since 2.0.0
 */
public class SqlWithParams {
    private final String sql;
    private final Map<String, Object> params;

    public SqlWithParams(String sql, Map<String, Object> params) {
        this.sql = sql;
        this.params = params;
    }

    public String getSql() {
        return sql;
    }

    public Map<String, Object> getParams() {
        return params;
    }
}
