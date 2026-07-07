package com.tbs.sql.properties;

import com.tbs.sql.assembler.ISqlAssembler;
import com.tbs.sql.assembler.impl.DefaultSqlAssembler;
import com.tbs.sql.logger.ISqlLogger;
import com.tbs.sql.logger.impl.NoSqlLogger;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SQL模块配置属性类，封装SQL模块的所有配置信息。
 *
 * <p>该类使用 {@link ConfigurationProperties} 注解绑定配置文件中的属性，
 * 提供SQL模块的所有可配置项，包括日志拦截器、自动填充拦截器、日志记录器等。</p>
 *
 * <h3>配置属性</h3>
 * <ul>
 *   <li>{@code enableLogInterceptor}: 是否启用SQL日志拦截器，默认false</li>
 *   <li>{@code enableAutoFillValueInterceptor}: 是否启用自动填充拦截器，默认false</li>
 *   <li>{@code sqlLoggerType}: SQL日志记录器类型，默认 {@link NoSqlLogger}</li>
 *   <li>{@code useRawValue}: 是否使用原始值，默认false</li>
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
 *     enable-log-interceptor: true
 *     enable-auto-fill-value-interceptor: true
 *     sql-logger-type: com.tbs.sql.logger.impl.SimpleSqlLogger
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
     * 是否启用SQL日志拦截器。
     * 启用后，SQL执行时会记录日志信息。
     * 默认值为 {@code false}。
     */
    private boolean enableLogInterceptor = false;

    /**
     * SQL日志记录器类型。
     * 用于指定实现 {@link ISqlLogger} 接口的类，负责记录SQL执行日志。
     * 默认使用 {@link NoSqlLogger}，即不记录任何日志。
     */
    private Class<? extends ISqlLogger> sqlLoggerType = NoSqlLogger.class;

    /**
     * 是否使用原始值。
     * 启用后，SQL参数将使用原始值而不是预编译语句的参数。
     * 默认值为 {@code false}。
     */
    private boolean useRawValue = false;

    /**
     * SQL组装器类型。
     * 用于指定实现 {@link ISqlAssembler} 接口的类，负责将SQL片段组装成完整语句。
     * 默认使用 {@link DefaultSqlAssembler}。
     */
    private Class<? extends ISqlAssembler> sqlAssemblerType = DefaultSqlAssembler.class;
}
