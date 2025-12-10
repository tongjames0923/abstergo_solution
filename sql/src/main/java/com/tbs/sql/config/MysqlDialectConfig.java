package com.tbs.sql.config;

import com.tbs.sql.framework.extractor.*;
import com.tbs.sql.framework.extractor.impls.dialect.ImportedDialect;
import com.tbs.sql.framework.extractor.impls.mysql.*;
import org.springframework.context.annotation.Bean;


/**
 * MySQL数据库方言配置类
 * <p>
 * 提供MySQL数据库的各种SQL提取器配置，包括分组、分页、选择、排序和条件提取器
 * 并创建MySQL SQL方言实例
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class MysqlDialectConfig {
    /**
     * 创建MySQL分组SQL提取器
     *
     * @return MySQL分组SQL提取器实例
     */
    @Bean
    public IGroupSqlExtractor groupSqlExtractor() {
        return new MysqlGroupExtractor();
    }

    /**
     * 创建MySQL分页SQL提取器
     *
     * @return MySQL分页SQL提取器实例
     */
    @Bean
    public ILimitSqlExtractor limitSqlExtractor() {
        return new MysqlLimitExtractor();
    }

    /**
     * 创建MySQL选择SQL提取器
     *
     * @return MySQL选择SQL提取器实例
     */
    @Bean
    public ISelectSqlExtractor selectSqlExtractor() {
        return new MysqlSelectExtractor();
    }

    /**
     * 创建MySQL排序SQL提取器
     *
     * @return MySQL排序SQL提取器实例
     */
    @Bean
    public IOrderSqlExtractor orderSqlExtractor() {
        return new MysqlOrderExtractor();
    }

    /**
     * 创建MySQL条件SQL提取器
     *
     * @return MySQL条件SQL提取器实例
     */
    @Bean
    public IWhereSqlExtractor whereSqlExtractor() {
        return new MysqlWhereExtractor();
    }

    /**
     * 创建MySQL SQL方言实例
     *
     * @param groupSqlExtractor 分组SQL提取器
     * @param limitSqlExtractor 分页SQL提取器
     * @param selectSqlExtractor 选择SQL提取器
     * @param orderSqlExtractor 排序SQL提取器
     * @param whereSqlExtractor 条件SQL提取器
     * @return MySQL SQL方言实例
     */
    @Bean
    public ISelectSqlDialect selectSqlDialect(IGroupSqlExtractor groupSqlExtractor,
        ILimitSqlExtractor limitSqlExtractor, ISelectSqlExtractor selectSqlExtractor,
        IOrderSqlExtractor orderSqlExtractor, IWhereSqlExtractor whereSqlExtractor) {
        return new ImportedDialect(groupSqlExtractor, orderSqlExtractor, limitSqlExtractor, selectSqlExtractor,
            whereSqlExtractor);
    }
}
