package com.tbs.sql.datasource.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JDBC 连接获取阶段的拦截上下文。
 *
 * <p>由 {@code TbsDataSourceDecorator} 在 {@code getConnection} 系方法入口构造，
 * 传递给 {@link com.tbs.sql.datasource.interceptor.JdbcExecutionInterceptor#interceptConnection}。
 * 该上下文仅用于观测与裁决，不承担数据源路由职责。</p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JdbcConnectionContext {

    /**
     * 数据源标识（被装饰 DataSource 的 Bean 名称）
     */
    private String dataSourceId;

    /**
     * 连接用户名，无参 getConnection 时为 null
     */
    private String userName;

    /**
     * 发起连接的线程名
     */
    private String threadName;

    /**
     * 连接获取请求时间（毫秒时间戳）
     */
    private long acquireTime;
}
