package com.tbs.sql.autoconfig;

import com.tbs.sql.assembler.ISqlAssembler;
import com.tbs.sql.assembler.impl.DefaultSqlAssembler;
import com.tbs.sql.dialect.*;
import com.tbs.sql.dialect.extractor.CommonGroupExtractor;
import com.tbs.sql.dialect.extractor.CommonOrderExtractor;
import com.tbs.sql.dialect.extractor.CommonWhereExtractor;
import com.tbs.sql.dialect.postgresql.PostgreSqlDialect;
import com.tbs.sql.dialect.postgresql.extractor.PostgreSqlLimitExtractor;
import com.tbs.sql.dialect.postgresql.extractor.PostgreSqlSelectExtractor;
import org.springframework.context.annotation.Bean;

/**
 * PostgreSQL方言自动配置类，注册PostgreSQL数据库特有的SQL提取器和方言实现。
 *
 * <p>该类负责配置PostgreSQL数据库的方言支持，包括SELECT、WHERE、ORDER BY、GROUP BY、
 * LIMIT等SQL片段的提取器，以及PostgreSQL方言的完整实现。其中 SELECT 与分页提取器为
 * PostgreSQL 专有实现，WHERE / ORDER BY / GROUP BY 复用 Common 系列实现。</p>
 *
 * <h3>注册的Bean</h3>
 * <ul>
 *   <li>{@link PostgreSqlSelectExtractor} — SELECT语句提取器（PostgreSQL 专有实现）</li>
 *   <li>{@link CommonWhereExtractor} — WHERE条件提取器（共用实现，以"PostgreSQL"作为方言标识构造）</li>
 *   <li>{@link CommonOrderExtractor} — ORDER BY语句提取器（各方言共用实现）</li>
 *   <li>{@link CommonGroupExtractor} — GROUP BY语句提取器（各方言共用实现）</li>
 *   <li>{@link PostgreSqlLimitExtractor} — LIMIT/OFFSET分页提取器（PostgreSQL 专有实现）</li>
 *   <li>{@link DefaultSqlAssembler} — 默认SQL组装器</li>
 *   <li>{@link PostgreSqlDialect} — PostgreSQL方言实现</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see com.tbs.sql.annotation.dialect.UsePostgreSql
 * @see PostgreSqlDialect
 */
public class PostgreSqlDialectAutoConfiguration {

    @Bean
    public IGroupExtractor groupExtractor() {
        return new CommonGroupExtractor();
    }

    @Bean
    public ILimitExtractor limitExtractor() {
        return new PostgreSqlLimitExtractor();
    }

    @Bean
    public ISelectExtractor selectExtractor() {
        return new PostgreSqlSelectExtractor();
    }

    @Bean
    public IOrderExtractor orderExtractor() {
        return new CommonOrderExtractor();
    }

    @Bean
    public IWhereExtractor whereExtractor() {
        return new CommonWhereExtractor("PostgreSQL");
    }

    @Bean
    public ISqlAssembler systemSqlAssembler() {
        return new DefaultSqlAssembler();
    }

    @Bean
    public SqlDialect sqlDialect(IGroupExtractor groupExtractor, ILimitExtractor limitExtractor,
            ISelectExtractor selectExtractor, IOrderExtractor orderExtractor,
            IWhereExtractor whereExtractor) {
        return new PostgreSqlDialect(groupExtractor, orderExtractor, limitExtractor,
            selectExtractor, whereExtractor);
    }
}
