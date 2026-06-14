package com.tbs.sql.annotation.dialect;

import com.tbs.sql.autoconfig.MysqlDialectAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 启用MySQL数据库方言注解。
 *
 * <p>该注解用于在Spring Boot应用中启用MySQL数据库方言支持，包括MySQL特有的
 * SQL语法、分页、排序等功能。当使用MySQL数据库时，需要添加此注解。</p>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>使用MySQL数据库时</li>
 *   <li>需要MySQL特有的SQL语法支持时</li>
 *   <li>需要使用MySQL分页功能时</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @EnableTbsSql
 * @UseMysql
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
 *   <li>该注解会自动导入 {@link com.tbs.sql.autoconfig.MysqlDialectAutoConfiguration} 配置类</li>
 *   <li>需要配合 {@link EnableTbsSql} 注解使用</li>
 *   <li>目前仅支持MySQL数据库，其他数据库需要实现相应的方言</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see com.tbs.sql.autoconfig.MysqlDialectAutoConfiguration
 * @see com.tbs.sql.dialect.mysql.MysqlDialect
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({MysqlDialectAutoConfiguration.class})
public @interface UseMysql {
}
