package com.tbs.sql.annotation.core;

import com.tbs.sql.autoconfig.SqlAutoConfiguration;
import com.tbs.sql.annotation.dialect.UseMysql;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 启用TBS SQL框架注解。
 *
 * <p>该注解用于在Spring Boot应用中启用TBS SQL框架的所有功能，包括自动配置、
 * 事务管理、SQL查询构建等。通常添加在应用的主启动类上。</p>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>在Spring Boot应用中启用TBS SQL框架</li>
 *   <li>需要使用SQL查询构建功能时</li>
 *   <li>需要使用自动填充值、日志记录等高级功能时</li>
 *   <li>需要 DataSource 装饰与 JDBC 执行拦截能力时（另需 {@code tbs.sql.enable-datasource-intercept=true}）</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @EnableTbsSql
 * @SpringBootApplication
 * public class Application {
 *     public static void main(String[] args) {
 *         SpringApplication.run(Application.class, args);
 *     }
 * }
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>该注解会自动导入 {@link SqlAutoConfiguration} 配置类</li>
 *   <li>需要配合 {@code application.yml} 中的配置使用</li>
 *   <li>默认启用事务管理功能</li>
 *   <li>DataSource 装饰默认关闭，开启后容器内主数据源被装饰，注册为 Spring Bean 的
 *       {@link com.tbs.sql.datasource.interceptor.JdbcExecutionInterceptor} 链式生效</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see SqlAutoConfiguration
 * @see com.tbs.sql.properties.SqlProperty
 */
@EnableTransactionManagement
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({SqlAutoConfiguration.class})
@EnableConfigurationProperties({com.tbs.sql.properties.SqlProperty.class})
public @interface EnableTbsSql {
}
