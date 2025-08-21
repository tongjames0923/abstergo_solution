package com.tbs.sql.config;

import javax.annotation.Resource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.transaction.PlatformTransactionManager;

import com.tbs.common.util.BeanUtil;
import com.tbs.sql.aop.FrameworkMapperAOP;
import com.tbs.sql.constants.OrderConstant;
import com.tbs.sql.ef.IEntityQueryWrapper;
import com.tbs.sql.ef.impl.SimpleEntityQueryWrapper;
import com.tbs.sql.extractor.ISelectSqlDialect;
import com.tbs.sql.logger.ISqlLogger;
import com.tbs.sql.logger.impl.NoSqlLogger;
import com.tbs.sql.mapper.impl.EntitySelectFetchMapper;
import com.tbs.sql.mapper.impl.FetchMapper;
import com.tbs.sql.order.impl.AscStaticOrder;
import com.tbs.sql.order.impl.DescStaticOrder;
import com.tbs.sql.order.impl.OrderedModelOrderImpl;
import com.tbs.sql.properties.SqlProperty;
import com.tbs.sql.query.ISqlAssembler;
import com.tbs.sql.query.impl.DefaultSqlAssembler;
import com.tbs.sql.utils.QueryUtil;
import com.tbs.sql.utils.TransactionUtil;

import tk.mybatis.spring.annotation.MapperScan;

/**
 * @author abstergo
 */
@MapperScan(basePackageClasses = EntitySelectFetchMapper.class)
public class SqlConfig {

	@Resource
	SqlProperty sqlProperty;

	@Bean
	FrameworkMapperAOP frameworkMapperAOP() {
		return new FrameworkMapperAOP();
	}

	@Bean
	FetchMapper fetchMapper() {
		return new FetchMapper();
	}

	@Bean
	public ISqlAssembler systemSqlAssembler() throws Throwable {
		return BeanUtil.buildBeanFromProperties(() -> {
			return new DefaultSqlAssembler();
		}, sqlProperty.getSqlAssemblerType(), BeanUtil::buildByEmptyArgs);
	}

	@Bean
	public QueryUtil sqlUtil(ISelectSqlDialect dialect, ISqlAssembler sqlAssembler) {
		return new QueryUtil(dialect, sqlProperty, sqlAssembler);
	}

	@Bean
	public TransactionUtil transactionUtil(PlatformTransactionManager transactionManager) {
		return new TransactionUtil(transactionManager);
	}

	@Bean
	@ConditionalOnProperty(name = "tbs.sql.enable-log-interceptor", havingValue = "true")
	SqlLoggingInterceptor sqlLoggingInterceptor() {
		return new SqlLoggingInterceptor();
	}

	@Bean
	@ConditionalOnProperty(name = "tbs.sql.enable-auto-fill-value-interceptor", havingValue = "true")
	SqlAutoFillIntercepter autoFillIntercepter() {
		return new SqlAutoFillIntercepter();
	}

	@Bean
	@ConditionalOnMissingBean(ISqlLogger.class)
	@ConditionalOnBean(SqlLoggingInterceptor.class)
	public ISqlLogger defaultSqlLogger() throws Throwable {
		return BeanUtil.buildBeanFromProperties(() -> {
			return new NoSqlLogger();
		}, sqlProperty.getSqlLoggerType(), BeanUtil::buildByEmptyArgs);
	}

	@Bean(OrderConstant.ASC)
	public AscStaticOrder ascOrder() {
		return new AscStaticOrder();
	}

	@Bean(OrderConstant.DESC)
	public DescStaticOrder descOrder() {
		return new DescStaticOrder();
	}

	@Bean(OrderConstant.ORDERED_MODEL)
	public OrderedModelOrderImpl orderedModelOrderImpl() {
		return new OrderedModelOrderImpl();
	}

	@Bean
	@Scope("prototype")
	IEntityQueryWrapper entityWrapper() {
		return new SimpleEntityQueryWrapper();
	}
}
