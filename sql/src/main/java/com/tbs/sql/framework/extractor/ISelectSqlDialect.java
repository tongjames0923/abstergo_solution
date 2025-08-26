package com.tbs.sql.framework.extractor;

/**
 * SQL查询语句方言
 *
 * @author abstergo
 */
public interface ISelectSqlDialect {
    /**
     * 获取分组语句提取器
     *
     * @return
     */
    IGroupSqlExtractor getGroupSqlExtractor();

    /**
     * 获取排序语句提取器
     *
     * @return
     */
    IOrderSqlExtractor getOrderSqlExtractor();

    /**
     * 获取分页语句提取器
     *
     * @return
     */
    ILimitSqlExtractor getLimitSqlExtractor();

    /**
     * 获取查询语句提取器
     *
     * @return
     */
    ISelectSqlExtractor getSelectSqlExtractor();

    /**
     * 获取条件语句提取器
     *
     * @return
     */
    IWhereSqlExtractor getWhereSqlExtractor();
}
