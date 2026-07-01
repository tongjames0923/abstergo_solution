package com.tbs.sql.annotation.dialect;

import com.tbs.sql.autoconfig.SqlServerDialectAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 启用SQL Server数据库方言注解。
 *
 * <p>该注解用于在Spring Boot应用中启用SQL Server数据库方言支持，包括SQL Server特有的
 * SQL语法、分页（OFFSET/FETCH NEXT）、排序等功能。当使用SQL Server数据库时，需要添加此注解。</p>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>使用SQL Server数据库时</li>
 *   <li>需要SQL Server特有的SQL语法支持时</li>
 *   <li>需要使用SQL Server分页功能（OFFSET/FETCH NEXT）时</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @EnableTbsSql
 * @UseSqlServer
 * @SpringBootApplication
 * public class Application {
 *     public static void main(String[] args) {
 *         SpringApplication.run(Application.class, args);
 *     }
 * }
 * }</pre>
 *
 * <h3>与MySQL的对比</h3>
 * <ul>
 *   <li>MySQL使用 {@link com.tbs.sql.annotation.dialect.UseMysql} 注解</li>
 *   <li>SQL Server使用本注解 {@code @UseSqlServer}</li>
 *   <li>两者互斥，同一应用只需启用其中一个</li>
 * </ul>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>该注解会自动导入 {@link SqlServerDialectAutoConfiguration} 配置类</li>
 *   <li>需要配合 {@link com.tbs.sql.annotation.core.EnableTbsSql} 注解使用</li>
 *   <li>SQL Server需要2012或更高版本以支持OFFSET/FETCH分页语法</li>
 *   <li>分页查询时框架会自动补充ORDER BY (SELECT NULL) 以满足SQL Server语法要求</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see SqlServerDialectAutoConfiguration
 * @see com.tbs.sql.dialect.sqlserver.SqlServerDialect
 * @see com.tbs.sql.annotation.dialect.UseMysql
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({SqlServerDialectAutoConfiguration.class})
public @interface UseSqlServer {
}
