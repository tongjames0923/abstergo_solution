package com.tbs.sql.autoconfig;

import com.tbs.sql.datasource.decoration.DataSourceDecorationPostProcessor;
import com.tbs.sql.datasource.decoration.InterceptorChain;
import com.tbs.sql.datasource.decoration.SqlExcludeRule;
import com.tbs.sql.datasource.interceptor.JdbcExecutionInterceptor;
import com.tbs.sql.datasource.interceptor.impl.SqlExecutionLogInterceptor;
import com.tbs.sql.properties.SqlProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * DataSource 装饰与 JDBC 执行拦截的条件化装配类。
 *
 * <p>由 {@link SqlAutoConfiguration} 导入，满足 {@code tbs.sql.enable-datasource-intercept=true}
 * （默认关闭）时生效；装饰目标的选取（唯一候选 / @Primary）由
 * {@link DataSourceDecorationPostProcessor} 在运行时判定，避免在用户配置导入链中
 * 对数据源自动配置求值导致的时序误判。</p>
 *
 * <p>拦截器列表经 {@link InterceptorChain} 的 Supplier 惰性解析，
 * 用户自定义的 {@link JdbcExecutionInterceptor}（Spring Bean）无需关心装配顺序。</p>
 *
 * <p>同时注册内置观测拦截器 {@link SqlExecutionLogInterceptor}，以 JDBC 装饰层为观测点
 * 输出 SQL、执行耗时与慢 SQL 告警，替代驱动层 p6spy 代理驱动。</p>
 *
 * @author abstergo
 * @since 1.0.0
 * @see JdbcExecutionInterceptor
 */
@Configuration
@ConditionalOnProperty(name = "tbs.sql.enable-datasource-intercept", havingValue = "true")
public class DataSourceDecorationConfiguration {

    /**
     * 注册 DataSource 装饰后置处理器。
     *
     * <p>以 static {@code @Bean} 注册，确保后置处理器在任何单例实例化之前完成注册，
     * 避免装饰漏掉目标 Bean。</p>
     *
     * @param applicationContext Spring 上下文，用于惰性解析拦截器 Bean
     * @param sqlProperty        SQL 模块配置属性
     * @return 装饰后置处理器
     */
    @Bean
    static DataSourceDecorationPostProcessor dataSourceDecorationPostProcessor(
            ApplicationContext applicationContext, SqlProperty sqlProperty) {
        InterceptorChain chain = new InterceptorChain(
                () -> resolveInterceptors(applicationContext),
                new SqlExcludeRule(sqlProperty.getExcludeSql()));
        return new DataSourceDecorationPostProcessor(chain);
    }

    /**
     * 注册内置 SQL 执行观测拦截器：输出 SQL、执行耗时与慢 SQL 告警。
     *
     * @param sqlProperty SQL 模块配置属性
     * @return SQL 执行观测拦截器
     */
    @Bean
    SqlExecutionLogInterceptor sqlExecutionLogInterceptor(SqlProperty sqlProperty) {
        return new SqlExecutionLogInterceptor(sqlProperty.isLogSql(), sqlProperty.getSlowSqlThresholdMillis());
    }

    /**
     * 惰性解析容器内全部 {@link JdbcExecutionInterceptor} Bean。
     *
     * @param applicationContext Spring 上下文
     * @return 拦截器列表，无拦截器时返回空列表
     */
    private static List<JdbcExecutionInterceptor> resolveInterceptors(ApplicationContext applicationContext) {
        List<JdbcExecutionInterceptor> interceptors = new ArrayList<>(
                applicationContext.getBeansOfType(JdbcExecutionInterceptor.class).values());
        interceptors.removeIf(Objects::isNull);
        return interceptors;
    }
}
