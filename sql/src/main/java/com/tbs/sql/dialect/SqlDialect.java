package com.tbs.sql.dialect;

/**
 * SQL方言接口
 *
 * @author abstergo
 * @since 2.0.0
 */
public interface SqlDialect {
    IGroupExtractor getGroupExtractor();
    IOrderExtractor getOrderExtractor();
    ILimitExtractor getLimitExtractor();
    ISelectExtractor getSelectExtractor();
    IWhereExtractor getWhereExtractor();
    /** 方言名称标识 */
    default String name() { return "default"; }
}
