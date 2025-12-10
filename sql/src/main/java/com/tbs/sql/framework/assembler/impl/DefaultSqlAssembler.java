package com.tbs.sql.framework.assembler.impl;

import com.tbs.sql.framework.assembler.ISqlAssembler;

/**
 * 默认的SQL组装器实现类
 * <p>
 * 实现ISqlAssembler接口，将各个SQL片段用空格连接成完整的SQL语句
 * </p>
 *
 * @author tbs
 * @since 1.0.0
 */
public class DefaultSqlAssembler implements ISqlAssembler {
    /**
     * 空格字符串常量
     */
    private static final String SPACE = " ";

    /**
     * 组装查询SQL语句
     * <p>
     * 将选择、条件、排序、分组和分页等SQL片段用空格连接成完整的查询SQL语句
     * </p>
     *
     * @param selectSql 选择SQL片段
     * @param whereSql 条件SQL片段
     * @param orderSql 排序SQL片段
     * @param groupSql 分组SQL片段
     * @param limitSql 分页SQL片段
     * @return 完整的查询SQL语句
     */
    @Override
    public String assembleSelectSql(String selectSql, String whereSql, String orderSql, String groupSql, String limitSql) {
        return selectSql + SPACE + whereSql + SPACE + groupSql + SPACE + orderSql + SPACE + limitSql;
    }
}
