package com.tbs.sql.autoconfig;

import com.tbs.sql.dialect.*;
import com.tbs.sql.dialect.mysql.MysqlDialect;
import com.tbs.sql.dialect.mysql.extractor.*;
import org.springframework.context.annotation.Bean;

/**
 * MySQL方言自动配置
 *
 * @author abstergo
 * @since 2.0.0
 */
public class MysqlDialectAutoConfiguration {
    @Bean public IGroupExtractor groupExtractor() { return new MysqlGroupExtractor(); }
    @Bean public ILimitExtractor limitExtractor() { return new MysqlLimitExtractor(); }
    @Bean public ISelectExtractor selectExtractor() { return new MysqlSelectExtractor(); }
    @Bean public IOrderExtractor orderExtractor() { return new MysqlOrderExtractor(); }
    @Bean public IWhereExtractor whereExtractor() { return new MysqlWhereExtractor(); }

    @Bean
    public SqlDialect sqlDialect(IGroupExtractor groupExtractor, ILimitExtractor limitExtractor,
            ISelectExtractor selectExtractor, IOrderExtractor orderExtractor, IWhereExtractor whereExtractor) {
        return new MysqlDialect(groupExtractor, orderExtractor, limitExtractor, selectExtractor, whereExtractor);
    }
}
