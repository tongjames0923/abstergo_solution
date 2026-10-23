package com.tbs.sql.properties;

import com.tbs.sql.assembler.ISqlAssembler;
import com.tbs.sql.assembler.impl.DefaultSqlAssembler;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * SQL模块配置属性类，封装SQL模块的所有配置信息。
 *
 * <p>该类使用 {@link ConfigurationProperties} 注解绑定配置文件中的属性，
 * 提供SQL模块的所有可配置项，包括日志拦截器、自动填充拦截器、日志记录器等。</p>
 *
 * <h3>配置属性</h3>
 * <ul>
 *   <li>{@code enableDatasourceIntercept}: 是否启用 DataSource 装饰与 JDBC 执行拦截，默认false</li>
 *   <li>{@code logSql}: 是否输出全量 SQL 执行日志，默认true</li>
 *   <li>{@code slowSqlThresholdMillis}: 慢 SQL 告警阈值（毫秒），默认2000</li>
 *   <li>{@code logParameters}: 是否在 SQL 执行日志中输出参数值，默认true</li>
 *   <li>{@code sqlAssemblerType}: SQL组装器类型，默认 {@link DefaultSqlAssembler}</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>配置SQL模块的行为</li>
 *   <li>启用或禁用功能模块</li>
 *   <li>自定义日志记录器和SQL组装器</li>
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
 *     log-parameters: true
 *     sql-assembler-type: com.tbs.sql.assembler.impl.DefaultSqlAssembler
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>配置属性前缀为 {@code tbs.sql}</li>
 *   <li>修改配置后需要重启应用</li>
 *   <li>日志记录器和组装器类型需要是实现类的全限定名</li>
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
     * 是否在 SQL 执行日志中输出 PreparedStatement 参数值。
     * 开启后可定位具体数据行，但可能将敏感信息写入日志，生产环境可按需关闭。
     * 默认值为 {@code true}。
     */
    private boolean logParameters = true;

    /**
     * 是否启用原始 SQL 模式。
     * 启用后将禁用所有 SQL 片段组装，直接使用原始 SQL 字符串。
     * 默认值为 {@code false}。
     */
    private boolean useRawValue = false;

    /**
     * SQL组装器类型。
     * 用于指定实现 {@link ISqlAssembler} 接口的类，负责将SQL片段组装成完整语句。
     * 默认使用 {@link DefaultSqlAssembler}。
     */
    private Class<? extends ISqlAssembler> sqlAssemblerType = DefaultSqlAssembler.class;

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
