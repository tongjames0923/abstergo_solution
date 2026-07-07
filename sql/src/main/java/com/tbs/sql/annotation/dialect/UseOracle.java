package com.tbs.sql.annotation.dialect;

import com.tbs.sql.autoconfig.OracleDialectAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 启用Oracle数据库方言注解。
 *
 * <p>该注解用于在Spring Boot应用中启用Oracle数据库方言支持，包括Oracle特有的
 * SQL语法、分页（OFFSET/FETCH NEXT）、排序等功能。当使用Oracle数据库时，需要添加此注解。</p>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>使用Oracle数据库时</li>
 *   <li>需要Oracle特有的SQL语法支持时</li>
 *   <li>需要使用Oracle分页功能（OFFSET/FETCH NEXT，需Oracle 12c+）时</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @EnableTbsSql
 * @UseOracle
 * @SpringBootApplication
 * public class Application {
 *     public static void main(String[] args) {
 *         SpringApplication.run(Application.class, args);
 *     }
 * }
 * }</pre>
 *
 * <h3>与其他方言注解的对比</h3>
 * <ul>
 *   <li>MySQL使用 {@link UseMysql} 注解</li>
 *   <li>SQL Server使用 {@link UseSqlServer} 注解</li>
 *   <li>PostgreSQL使用 {@link UsePostgreSql} 注解</li>
 *   <li>各方言注解互斥，同一应用只需启用其中一个</li>
 * </ul>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>该注解会自动导入 {@link OracleDialectAutoConfiguration} 配置类</li>
 *   <li>需要配合 {@link com.tbs.sql.annotation.core.EnableTbsSql} 注解使用</li>
 *   <li>Oracle需要12c或更高版本以支持OFFSET/FETCH分页语法</li>
 *   <li>分页查询时框架会自动补充ORDER BY (SELECT NULL FROM DUAL) 以满足Oracle语法要求</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see OracleDialectAutoConfiguration
 * @see com.tbs.sql.dialect.oracle.OracleDialect
 * @see UseMysql
 * @see UseSqlServer
 * @see UsePostgreSql
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({OracleDialectAutoConfiguration.class})
public @interface UseOracle {
}
