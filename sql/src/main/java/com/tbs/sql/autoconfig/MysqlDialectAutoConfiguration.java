package com.tbs.sql.autoconfig;

import com.tbs.sql.assembler.ISqlAssembler;
import com.tbs.sql.assembler.impl.DefaultSqlAssembler;
import com.tbs.sql.dialect.*;
import com.tbs.sql.dialect.extractor.CommonGroupExtractor;
import com.tbs.sql.dialect.extractor.CommonOrderExtractor;
import com.tbs.sql.dialect.extractor.CommonSelectExtractor;
import com.tbs.sql.dialect.extractor.CommonWhereExtractor;
import com.tbs.sql.dialect.mysql.MysqlDialect;
import com.tbs.sql.dialect.mysql.extractor.MysqlLimitExtractor;
import org.springframework.context.annotation.Bean;

/**
 * MySQL方言自动配置类，注册MySQL数据库特有的SQL提取器和方言实现。
 *
 * <p>该类负责配置MySQL数据库的方言支持，包括SELECT、WHERE、ORDER BY、GROUP BY、
 * LIMIT等SQL片段的提取器，以及MySQL方言的完整实现。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>注册MySQL SELECT提取器 {@link MysqlSelectExtractor}</li>
 *   <li>注册MySQL WHERE提取器 {@link MysqlWhereExtractor}</li>
 *   <li>注册MySQL ORDER BY提取器 {@link MysqlOrderExtractor}</li>
 *   <li>注册MySQL GROUP BY提取器 {@link MysqlGroupExtractor}</li>
 *   <li>注册MySQL LIMIT提取器 {@link MysqlLimitExtractor}</li>
 *   <li>注册MySQL方言实现 {@link MysqlDialect}</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>使用MySQL数据库时需要启用</li>
 *   <li>通过 {@link com.tbs.sql.annotation.dialect.UseMysql} 注解启用</li>
 * </ul>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>需要配合 {@link com.tbs.sql.annotation.core.EnableTbsSql} 注解使用</li>
 *   <li>仅适用于MySQL数据库</li>
 *   <li>如果需要支持其他数据库，需要实现相应的方言配置类</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see com.tbs.sql.annotation.dialect.UseMysql
 * @see com.tbs.sql.dialect.mysql.MysqlDialect
 */
public class MysqlDialectAutoConfiguration {
    @Bean public IGroupExtractor groupExtractor() { return new CommonGroupExtractor(); }
    @Bean public ILimitExtractor limitExtractor() { return new MysqlLimitExtractor(); }
    @Bean public ISelectExtractor selectExtractor() { return new CommonSelectExtractor(); }
    @Bean public IOrderExtractor orderExtractor() { return new CommonOrderExtractor(); }
    @Bean public IWhereExtractor whereExtractor() { return new CommonWhereExtractor("MySQL"); }

    @Bean
    public ISqlAssembler systemSqlAssembler() throws Throwable {
        return new DefaultSqlAssembler();
    }

    @Bean
    public SqlDialect sqlDialect(IGroupExtractor groupExtractor, ILimitExtractor limitExtractor,
            ISelectExtractor selectExtractor, IOrderExtractor orderExtractor, IWhereExtractor whereExtractor) {
        return new MysqlDialect(groupExtractor, orderExtractor, limitExtractor, selectExtractor, whereExtractor);
    }
}
