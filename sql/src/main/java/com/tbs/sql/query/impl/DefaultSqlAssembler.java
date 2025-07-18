package com.tbs.sql.query.impl;

import com.tbs.sql.query.ISqlAssembler;

/**
 * 默认的SQL组装器
 *
 * @author tbs
 */
public class DefaultSqlAssembler implements ISqlAssembler {
    private static final String SPACE = " ";

    @Override
    public String assemble(String selectSql, String whereSql, String orderSql, String groupSql, String limitSql) {
        return selectSql + SPACE + whereSql + SPACE + groupSql + SPACE + orderSql + SPACE + limitSql;
    }
}
