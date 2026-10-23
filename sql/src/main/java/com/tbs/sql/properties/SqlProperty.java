package com.tbs.sql.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * SQL模块配置属性类，封装SQL模块的所有配置信息。
 *
 * <p>该类使用 {@link ConfigurationProperties} 注解绑定配置文件中的属性，
 * 提供 SQL 模块的全部可配置项：SQL 执行日志与慢 SQL 阈值、
 * DataSource 装饰与 JDBC 执行拦截开关、全表更新防护开关、SQL 排除前缀列表。</p>
 *
 * <h3>配置属性</h3>
 * <ul>
 *   <li>{@code logSql}: 是否输出全量 SQL 执行日志（INFO），默认true</li>
 *   <li>{@code slowSqlThresholdMillis}: 慢 SQL 告警阈值（毫秒），默认2000</li>
 *   <li>{@code enableDatasourceIntercept}: 是否启用 DataSource 装饰与 JDBC 执行拦截，默认false</li>
 *   <li>{@code enableFullTableUpdateBlock}: 是否启用内置全表 UPDATE/DELETE 防护拦截器，默认false</li>
 *   <li>{@code excludeSql}: SQL 排除前缀列表，命中即跳过拦截器链，默认空</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>配置SQL模块的行为</li>
 *   <li>启用或禁用功能模块</li>
 *   <li>控制 SQL 观测与 DataSource / JDBC 拦截行为</li>
 * </ul>
 *
 * <h3>配置示例</h3>
 * <pre>{@code
 * # application.yml
 * tbs:
 *   sql:
 *     enable-datasource-intercept: true
 *     log-sql: true
 *     slow-sql-threshold-millis: 2000
 *     enable-full-table-update-block: false
 *     exclude-sql:
 *       - "SELECT 1"
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>配置属性前缀为 {@code tbs.sql}</li>
 *   <li>修改配置后需要重启应用</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see com.tbs.sql.autoconfig.SqlAutoConfiguration
 */
@Data
@ConfigurationProperties("tbs.sql")
public class SqlProperty {
    /**
     * 是否输出全量 SQL 执行日志（INFO 级别，含执行耗时）。
     * 慢 SQL 与执行失败的日志不受该开关控制，始终输出。
     * 默认值为 {@code true}。
     */
    private boolean logSql = true;

    /**
     * 慢 SQL 告警阈值（毫秒）。
     * 语句执行耗时达到该值时以 WARN 级别输出慢 SQL 告警。
     * 默认值为 {@code 2000}。
     */
    private long slowSqlThresholdMillis = 2000L;

    /**
     * 是否启用 DataSource 装饰与 JDBC 执行拦截。
     * 启用后容器内的 DataSource 将被装饰，注册为 Spring Bean 的
     * {@link com.tbs.sql.datasource.interceptor.JdbcExecutionInterceptor} 按 order 升序链式生效。
     * 默认值为 {@code false}。
     */
    private boolean enableDatasourceIntercept = false;

    /**
     * 是否启用内置的全表 UPDATE/DELETE 防护拦截器。
     * 启用后拒绝无 WHERE 条件的 UPDATE / DELETE 语句。
     * 默认值为 {@code false}。
     */
    private boolean enableFullTableUpdateBlock = false;

    /**
     * SQL 排除前缀列表（忽略大小写，匹配前剥离头部注释与空白）。
     * 命中的 SQL 直接透传执行，跳过 JDBC 执行拦截器链，
     * 用于排除连接池健康检查、批量任务框架自身语句等场景。
     * 默认为空列表，即不排除任何 SQL。
     */
    private List<String> excludeSql = new ArrayList<>();
}
