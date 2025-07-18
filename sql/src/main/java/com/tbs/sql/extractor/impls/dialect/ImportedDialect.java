package com.tbs.sql.extractor.impls.dialect;

import com.tbs.sql.extractor.*;

/**
 * mysql 查询Sql方言
 *
 * @author abstergo
 */
public class ImportedDialect implements ISelectSqlDialect {
    private IGroupSqlExtractor groupSqlExtractor;
    private IOrderSqlExtractor orderSqlExtractor;
    private ILimitSqlExtractor limitSqlExtractor;
    private ISelectSqlExtractor selectSqlExtractor;
    private IWhereSqlExtractor whereSqlExtractor;

    public ImportedDialect(IGroupSqlExtractor groupSqlExtractor, IOrderSqlExtractor orderSqlExtractor,
        ILimitSqlExtractor limitSqlExtractor, ISelectSqlExtractor selectSqlExtractor,
        IWhereSqlExtractor whereSqlExtractor) {
        this.groupSqlExtractor = groupSqlExtractor;
        this.orderSqlExtractor = orderSqlExtractor;
        this.limitSqlExtractor = limitSqlExtractor;
        this.selectSqlExtractor = selectSqlExtractor;
        this.whereSqlExtractor = whereSqlExtractor;
    }

    @Override
    public IGroupSqlExtractor getGroupSqlExtractor() {
        return groupSqlExtractor;
    }

    @Override
    public IOrderSqlExtractor getOrderSqlExtractor() {
        return orderSqlExtractor;
    }

    @Override
    public ILimitSqlExtractor getLimitSqlExtractor() {
        return limitSqlExtractor;
    }

    @Override
    public ISelectSqlExtractor getSelectSqlExtractor() {
        return selectSqlExtractor;
    }

    @Override
    public IWhereSqlExtractor getWhereSqlExtractor() {
        return whereSqlExtractor;
    }
}
