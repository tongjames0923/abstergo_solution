package com.tbs.sql.datasource.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JDBC SQL 执行阶段的拦截上下文。
 *
 * <p>由 Statement 代理在 execute 系方法调用前构造，传递给
 * {@link com.tbs.sql.datasource.interceptor.JdbcExecutionInterceptor#interceptExecution}。</p>
 *
 * <h3>能力边界</h3>
 * <ul>
 *   <li>仅包含最终 SQL 文本（含 {@code ?} 占位符），<b>不包含</b> PreparedStatement 参数值</li>
 *   <li>SQL 文本仅供只读裁决使用，框架<b>不提供</b>改写能力</li>
 *   <li>批量执行时按批次内逐条 SQL 分别构造上下文</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JdbcExecuteContext {

    /**
     * 待执行的 SQL 文本（含占位符，未被改写）
     */
    private String sql;

    /**
     * 推断出的命令类型，参见 {@link SqlCommandKind}
     */
    private SqlCommandKind commandKind;

    /**
     * 数据源标识（被装饰 DataSource 的 Bean 名称）
     */
    private String dataSourceId;

    /**
     * 执行 SQL 的线程名
     */
    private String threadName;

    /**
     * 是否为批量执行（executeBatch）中的单条语句
     */
    private boolean batch;
}
