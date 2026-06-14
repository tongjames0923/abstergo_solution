package com.tbs.sql.assembler.impl;

import com.tbs.sql.assembler.ISqlAssembler;

import java.util.StringJoiner;

/**
 * 默认SQL组装器实现
 *
 * @author abstergo
 * @since 1.0.0
 */
public class DefaultSqlAssembler implements ISqlAssembler {
    private static final String SPACE = " ";

    @Override
    public String assembleSelectSql(String selectSql, String whereSql, String orderSql, String groupSql, String limitSql) {
        StringJoiner joiner = new StringJoiner(SPACE);
        if (selectSql != null && !selectSql.isEmpty()) joiner.add(selectSql);
        if (whereSql != null && !whereSql.isEmpty()) joiner.add(whereSql);
        if (groupSql != null && !groupSql.isEmpty()) joiner.add(groupSql);
        if (orderSql != null && !orderSql.isEmpty()) joiner.add(orderSql);
        if (limitSql != null && !limitSql.isEmpty()) joiner.add(limitSql);
        return joiner.toString();
    }
}
