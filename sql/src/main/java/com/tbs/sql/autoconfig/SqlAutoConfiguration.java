package com.tbs.sql.autoconfig;

import com.tbs.common.util.base.BeanUtil;
import com.tbs.sql.assembler.ISqlAssembler;
import com.tbs.sql.assembler.impl.DefaultSqlAssembler;
import com.tbs.sql.constants.OrderConstants;
import com.tbs.sql.dsl.IEntityQueryWrapper;
import com.tbs.sql.dsl.impl.SimpleEntityQueryWrapper;
import com.tbs.sql.dialect.SqlDialect;
import com.tbs.sql.interceptor.SqlAutoFillInterceptor;
import com.tbs.sql.interceptor.SqlLoggingInterceptor;
import com.tbs.sql.logger.ISqlLogger;
import com.tbs.sql.logger.impl.NoSqlLogger;
import com.tbs.sql.mapper.impl.FetchMapper;
import com.tbs.sql.order.impl.AscStaticOrder;
import com.tbs.sql.order.impl.DescStaticOrder;
import com.tbs.sql.order.impl.OrderedModelOrderImpl;
import com.tbs.sql.properties.SqlProperty;
import com.tbs.sql.service.QueryEngine;
import com.tbs.sql.util.TransactionUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.transaction.PlatformTransactionManager;
import tk.mybatis.spring.annotation.MapperScan;

import javax.annotation.Resource;

/**
 * SQL模块自动配置类
 *
 * @author abstergo
 * @since 2.0.0
 */
@MapperScan(basePackages = "com.tbs.sql.mapper")
public class SqlAutoConfiguration {

    @Resource
    SqlProperty sqlProperty;

    @Bean
    FetchMapper fetchMapper() { return new FetchMapper(); }

    @Bean
    public ISqlAssembler systemSqlAssembler() throws Throwable {
        return BeanUtil.buildBeanFromProperties(DefaultSqlAssembler::new, sqlProperty.getSqlAssemblerType(), BeanUtil::buildByEmptyArgs);
    }

    @Bean
    public QueryEngine queryEngine(SqlDialect dialect, ISqlAssembler sqlAssembler) {
        return new QueryEngine(dialect, sqlProperty, sqlAssembler);
    }

    @Bean
    public TransactionUtil transactionUtil(PlatformTransactionManager transactionManager) {
        return new TransactionUtil(transactionManager);
    }

    @Bean
    @ConditionalOnProperty(name = "tbs.sql.enable-log-interceptor", havingValue = "true")
    SqlLoggingInterceptor sqlLoggingInterceptor() { return new SqlLoggingInterceptor(); }

    @Bean
    @ConditionalOnProperty(name = "tbs.sql.enable-auto-fill-value-interceptor", havingValue = "true")
    SqlAutoFillInterceptor autoFillInterceptor() { return new SqlAutoFillInterceptor(); }

    @Bean
    @ConditionalOnMissingBean(ISqlLogger.class)
    @ConditionalOnBean(SqlLoggingInterceptor.class)
    public ISqlLogger defaultSqlLogger() throws Throwable {
        return BeanUtil.buildBeanFromProperties(NoSqlLogger::new, sqlProperty.getSqlLoggerType(), BeanUtil::buildByEmptyArgs);
    }

    @Bean(OrderConstants.ASC) public AscStaticOrder ascOrder() { return new AscStaticOrder(); }
    @Bean(OrderConstants.DESC) public DescStaticOrder descOrder() { return new DescStaticOrder(); }
    @Bean(OrderConstants.ORDERED_MODEL) public OrderedModelOrderImpl orderedModelOrderImpl() { return new OrderedModelOrderImpl(); }

    @Bean @Scope("prototype")
    IEntityQueryWrapper entityWrapper() { return new SimpleEntityQueryWrapper(); }
}
