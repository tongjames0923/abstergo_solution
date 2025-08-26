package com.tbs.sql.config;

import com.tbs.sql.framework.extractor.*;
import com.tbs.sql.framework.extractor.impls.dialect.ImportedDialect;
import com.tbs.sql.framework.extractor.impls.mysql.*;
import org.springframework.context.annotation.Bean;


/**
 * MySQL数据库配置
 *
 * @author abstergo
 */
public class MysqlDialectConfig {
    @Bean
    public IGroupSqlExtractor groupSqlExtractor() {
        return new MysqlGroupExtractor();
    }

    @Bean
    public ILimitSqlExtractor limitSqlExtractor() {
        return new MysqlLimitExtractor();
    }

    @Bean
    public ISelectSqlExtractor selectSqlExtractor() {
        return new MysqlSelectExtractor();
    }

    @Bean
    public IOrderSqlExtractor orderSqlExtractor() {
        return new MysqlOrderExtractor();
    }

    @Bean
    public IWhereSqlExtractor whereSqlExtractor() {
        return new MysqlWhereExtractor();
    }

    @Bean
    public ISelectSqlDialect selectSqlDialect(IGroupSqlExtractor groupSqlExtractor,
        ILimitSqlExtractor limitSqlExtractor, ISelectSqlExtractor selectSqlExtractor,
        IOrderSqlExtractor orderSqlExtractor, IWhereSqlExtractor whereSqlExtractor) {
        return new ImportedDialect(groupSqlExtractor, orderSqlExtractor, limitSqlExtractor, selectSqlExtractor,
            whereSqlExtractor);
    }
}
