package com.tbs.sql.autoconfig;

import com.tbs.sql.assembler.ISqlAssembler;
import com.tbs.sql.constants.OrderConstants;
import com.tbs.sql.datasource.interceptor.JdbcExecutionInterceptor;
import com.tbs.sql.datasource.interceptor.impl.FullTableUpdateBlockInterceptor;
import com.tbs.sql.dialect.SqlDialect;
import com.tbs.sql.dsl.IEntityQueryWrapper;
import com.tbs.sql.dsl.impl.SimpleEntityQueryWrapper;
import com.tbs.sql.i18n.SqlMessageSourceProvider;
import com.tbs.sql.interceptor.SqlAutoFillInterceptor;
import com.tbs.sql.mapper.impl.FetchMapper;
import com.tbs.sql.order.impl.AscStaticOrder;
import com.tbs.sql.order.impl.DescStaticOrder;
import com.tbs.sql.order.impl.OrderedModelOrderImpl;
import com.tbs.sql.properties.SqlProperty;
import com.tbs.sql.service.QueryEngine;
import com.tbs.sql.util.TransactionUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.transaction.PlatformTransactionManager;
import tk.mybatis.spring.annotation.MapperScan;

import jakarta.annotation.Resource;

/**
 * SQL模块自动配置类，负责注册和配置SQL框架的所有核心组件。
 *
 * <p>该类是TBS SQL框架的核心配置类，通过Spring Boot的自动配置机制，
 * 注册并配置SQL查询引擎、组装器、拦截器、日志记录器等核心组件。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>注册SQL查询引擎 {@link QueryEngine}</li>
 *   <li>注册SQL组装器 {@link ISqlAssembler}</li>
 *   <li>注册实体查询包装器 {@link IEntityQueryWrapper}</li>
 *   <li>注册 DataSource 装饰与 JDBC 执行拦截配置 {@link DataSourceDecorationConfiguration}</li>
 *   <li>配置自动填充值拦截器 {@link SqlAutoFillInterceptor}</li>
 *   <li>注册排序策略Bean</li>
 * </ul>
 *
 * <h3>配置说明</h3>
 * <p>需要在 {@code application.yml} 中配置以下属性：</p>
 * <pre>{@code
 * tbs:
 *   sql:
 *     enable-datasource-intercept: true        # 启用 DataSource 装饰与 JDBC 执行拦截
 *     log-sql: true                            # 输出全量 SQL 执行日志
 *     slow-sql-threshold-millis: 2000          # 慢 SQL 告警阈值（毫秒）
 *     sql-assembler-type: DefaultSqlAssembler  # SQL组装器类型
 * }</pre>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>在Spring Boot应用中自动配置SQL框架</li>
 *   <li>通过 {@link com.tbs.sql.annotation.core.EnableTbsSql} 注解启用</li>
 * </ul>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>需要配合 {@link com.tbs.sql.annotation.core.EnableTbsSql} 注解使用</li>
 *   <li>拦截器可以通过配置属性控制是否启用</li>
 *   <li>实体查询包装器使用原型作用域，每次请求都会创建新实例</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see QueryEngine
 * @see ISqlAssembler
 * @see com.tbs.sql.annotation.core.EnableTbsSql
 */
@MapperScan(basePackages = "com.tbs.sql.mapper", annotationClass = Mapper.class)
@Import(DataSourceDecorationConfiguration.class)
public class SqlAutoConfiguration {

    @Resource
    SqlProperty sqlProperty;

    /**
     * 注册 sql 模块消息源，使 datasource.intercept.* 拒绝文案可被全局 MessageSource 解析。
     */
    @Bean
    public SqlMessageSourceProvider sqlMessageSourceProvider() {
        return new SqlMessageSourceProvider();
    }

    @Bean
    FetchMapper fetchMapper() { return new FetchMapper(); }

    @Bean
    public QueryEngine queryEngine(SqlDialect dialect, ISqlAssembler sqlAssembler) {
        return new QueryEngine(dialect, sqlProperty, sqlAssembler);
    }

    @Bean
    public TransactionUtil transactionUtil(PlatformTransactionManager transactionManager) {
        return new TransactionUtil(transactionManager);
    }

    @Bean
    @ConditionalOnProperty(name = "tbs.sql.enable-full-table-update-block", havingValue = "true")
    JdbcExecutionInterceptor fullTableUpdateBlockInterceptor() {
        return new FullTableUpdateBlockInterceptor();
    }

    @Bean
    SqlAutoFillInterceptor autoFillInterceptor() { return new SqlAutoFillInterceptor(); }

    @Bean(OrderConstants.ASC) public AscStaticOrder ascOrder() { return new AscStaticOrder(); }
    @Bean(OrderConstants.DESC) public DescStaticOrder descOrder() { return new DescStaticOrder(); }
    @Bean(OrderConstants.ORDERED_MODEL) public OrderedModelOrderImpl orderedModelOrderImpl() { return new OrderedModelOrderImpl(); }

    @Bean @Scope("prototype")
    IEntityQueryWrapper entityWrapper() { return new SimpleEntityQueryWrapper(); }
}
