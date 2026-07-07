package com.tbs.sql.autoconfig;

import com.tbs.sql.assembler.ISqlAssembler;
import com.tbs.sql.dialect.*;
import com.tbs.sql.dialect.extractor.CommonGroupExtractor;
import com.tbs.sql.dialect.extractor.CommonOrderExtractor;
import com.tbs.sql.dialect.extractor.CommonSelectExtractor;
import com.tbs.sql.dialect.extractor.CommonWhereExtractor;
import com.tbs.sql.dialect.oracle.OracleAssembler;
import com.tbs.sql.dialect.oracle.OracleDialect;
import com.tbs.sql.dialect.oracle.extractor.OracleLimitExtractor;
import org.springframework.context.annotation.Bean;

/**
 * Oracle方言自动配置类，注册Oracle数据库特有的SQL提取器和方言实现。
 *
 * <p>该类负责配置Oracle数据库的方言支持，包括SELECT、WHERE、ORDER BY、GROUP BY、
 * OFFSET/FETCH等SQL片段的提取器，以及Oracle方言的完整实现。</p>
 *
 * <p>通过 {@link com.tbs.sql.annotation.dialect.UseOracle} 注解导入此配置类，
 * 自动注册所有Oracle所需的组件到Spring容器中。</p>
 *
 * <h3>注册的Bean</h3>
 * <ul>
 *   <li>{@link OracleSelectExtractor} — SELECT语句提取器</li>
 *   <li>{@link OracleWhereExtractor} — WHERE条件提取器</li>
 *   <li>{@link OracleOrderExtractor} — ORDER BY语句提取器</li>
 *   <li>{@link OracleGroupExtractor} — GROUP BY语句提取器</li>
 *   <li>{@link OracleLimitExtractor} — OFFSET/FETCH分页提取器</li>
 *   <li>{@link OracleAssembler} — Oracle专用组装器（处理分页时自动补充ORDER BY）</li>
 *   <li>{@link OracleDialect} — Oracle方言实现</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see com.tbs.sql.annotation.dialect.UseOracle
 * @see OracleDialect
 */
public class OracleDialectAutoConfiguration {

    @Bean
    public IGroupExtractor groupExtractor() {
        return new CommonGroupExtractor();
    }

    @Bean
    public ILimitExtractor limitExtractor() {
        return new OracleLimitExtractor();
    }

    @Bean
    public ISelectExtractor selectExtractor() {
        return new CommonSelectExtractor();
    }

    @Bean
    public IOrderExtractor orderExtractor() {
        return new CommonOrderExtractor();
    }

    @Bean
    public IWhereExtractor whereExtractor() {
        return new CommonWhereExtractor("Oracle");
    }

    @Bean
    public ISqlAssembler oracleAssembler() {
        return new OracleAssembler();
    }

    @Bean
    public SqlDialect sqlDialect(IGroupExtractor groupExtractor, ILimitExtractor limitExtractor,
            ISelectExtractor selectExtractor, IOrderExtractor orderExtractor,
            IWhereExtractor whereExtractor) {
        return new OracleDialect(groupExtractor, orderExtractor, limitExtractor,
            selectExtractor, whereExtractor);
    }
}
