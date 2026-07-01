package com.tbs.sql.autoconfig;

import com.tbs.sql.assembler.ISqlAssembler;
import com.tbs.sql.dialect.*;
import com.tbs.sql.dialect.sqlserver.SqlServerDialect;
import com.tbs.sql.dialect.sqlserver.SqlServerAssembler;
import com.tbs.sql.dialect.sqlserver.extractor.*;
import org.springframework.context.annotation.Bean;

/**
 * SQL Server方言自动配置类。
 *
 * <p>该类负责配置SQL Server数据库的方言支持，包括SELECT、WHERE、ORDER BY、GROUP BY、
 * OFFSET/FETCH等SQL片段的提取器，以及SQL Server方言的完整实现。</p>
 *
 * <p>通过 {@link com.tbs.sql.annotation.dialect.UseSqlServer} 注解导入此配置类，
 * 自动注册所有SQL Server所需的组件到Spring容器中。</p>
 *
 * <h3>注册的Bean</h3>
 * <ul>
 *   <li>{@link SqlServerSelectExtractor} — SELECT语句提取器</li>
 *   <li>{@link SqlServerWhereExtractor} — WHERE条件提取器</li>
 *   <li>{@link SqlServerOrderExtractor} — ORDER BY语句提取器</li>
 *   <li>{@link SqlServerGroupExtractor} — GROUP BY语句提取器</li>
 *   <li>{@link SqlServerLimitExtractor} — OFFSET/FETCH分页提取器</li>
 *   <li>{@link SqlServerAssembler} — SQL Server专用组装器（处理分页时自动补充ORDER BY）</li>
 *   <li>{@link SqlServerDialect} — SQL Server方言实现</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see com.tbs.sql.annotation.dialect.UseSqlServer
 * @see SqlServerDialect
 */
public class SqlServerDialectAutoConfiguration {

    @Bean
    public IGroupExtractor groupExtractor() {
        return new SqlServerGroupExtractor();
    }

    @Bean
    public ILimitExtractor limitExtractor() {
        return new SqlServerLimitExtractor();
    }

    @Bean
    public ISelectExtractor selectExtractor() {
        return new SqlServerSelectExtractor();
    }

    @Bean
    public IOrderExtractor orderExtractor() {
        return new SqlServerOrderExtractor();
    }

    @Bean
    public IWhereExtractor whereExtractor() {
        return new SqlServerWhereExtractor();
    }

    @Bean
    public ISqlAssembler sqlServerAssembler() {
        return new SqlServerAssembler();
    }

    @Bean
    public SqlDialect sqlDialect(IGroupExtractor groupExtractor, ILimitExtractor limitExtractor,
            ISelectExtractor selectExtractor, IOrderExtractor orderExtractor,
            IWhereExtractor whereExtractor) {
        return new SqlServerDialect(groupExtractor, orderExtractor, limitExtractor,
            selectExtractor, whereExtractor);
    }
}
