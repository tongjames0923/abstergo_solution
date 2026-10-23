package com.tbs.sql.datasource.interceptor.impl;

import com.tbs.sql.datasource.decoration.SqlTextUtils;
import com.tbs.sql.datasource.interceptor.JdbcExecutionInterceptor;
import com.tbs.sql.datasource.model.InterceptVerdict;
import com.tbs.sql.datasource.model.JdbcExecuteContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;

/**
 * 内置 SQL 执行观测拦截器：输出 SQL、执行耗时与慢 SQL 告警。
 *
 * <p>替代驱动层 p6spy 的 SQL 日志能力，观测点收敛到 JDBC 装饰层，
 * 因此日志可携带数据源标识、命令类型、批量标记与线程名。</p>
 *
 * <h3>输出策略</h3>
 * <ul>
 *   <li>执行失败：WARN，始终输出</li>
 *   <li>执行耗时达到慢 SQL 阈值：WARN（慢 SQL 告警），始终输出</li>
 *   <li>其余 SQL：INFO，由 {@code tbs.sql.log-sql} 控制是否输出</li>
 * </ul>
 *
 * <h3>能力边界</h3>
 * <p>只能看到带 {@code ?} 占位符的 SQL 文本，不包含 PreparedStatement 参数值；
 * 批量执行按整批耗时归并为一条日志。</p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class SqlExecutionLogInterceptor implements JdbcExecutionInterceptor {

    /**
     * SQL 观测专用日志器，独立名称便于单独配置日志级别
     */
    private static final Logger SQL_LOG = LoggerFactory.getLogger("datasource.sql.execution");

    /**
     * 单条日志中 SQL 文本的最大长度，超出截断，避免超长 IN 语句刷爆日志
     */
    private static final int MAX_SQL_LOG_LENGTH = 2000;

    /**
     * 截断后缀
     */
    private static final String TRUNCATED_SUFFIX = "...(truncated)";

    /**
     * 是否输出全量 SQL 执行日志
     */
    private final boolean logSql;

    /**
     * 慢 SQL 告警阈值（毫秒）
     */
    private final long slowSqlThresholdMillis;

    /**
     * 构造 SQL 执行观测拦截器。
     *
     * @param logSql                 是否输出全量 SQL 执行日志
     * @param slowSqlThresholdMillis 慢 SQL 告警阈值（毫秒）
     */
    public SqlExecutionLogInterceptor(boolean logSql, long slowSqlThresholdMillis) {
        this.logSql = logSql;
        this.slowSqlThresholdMillis = slowSqlThresholdMillis;
    }

    @Override
    public InterceptVerdict interceptExecution(JdbcExecuteContext context) {
        return InterceptVerdict.allow();
    }

    @Override
    public void onExecutionCompleted(JdbcExecuteContext context, long elapsedMillis, Throwable error) {
        if (error != null) {
            SQL_LOG.warn("[SQL] 执行失败: 耗时={}ms, 数据源={}, 类型={}, 批量={}, 线程={}, 异常={}, sql={}",
                    elapsedMillis, context.getDataSourceId(), context.getCommandKind(), context.isBatch(),
                    context.getThreadName(), error.toString(), sqlText(context));
            return;
        }
        if (elapsedMillis >= slowSqlThresholdMillis) {
            SQL_LOG.warn("[SQL] 慢 SQL 告警: 耗时={}ms, 阈值={}ms, 数据源={}, 类型={}, 批量={}, 线程={}, sql={}",
                    elapsedMillis, slowSqlThresholdMillis, context.getDataSourceId(), context.getCommandKind(),
                    context.isBatch(), context.getThreadName(), sqlText(context));
            return;
        }
        if (logSql) {
            SQL_LOG.info("[SQL] 执行完成: 耗时={}ms, 数据源={}, 类型={}, 批量={}, 线程={}, sql={}",
                    elapsedMillis, context.getDataSourceId(), context.getCommandKind(), context.isBatch(),
                    context.getThreadName(), sqlText(context));
        }
    }

    @Override
    public int getOrder() {
        // 观测拦截器不参与裁决顺序竞争，固定排在链尾
        return Ordered.LOWEST_PRECEDENCE;
    }

    /**
     * 单行化并截断 SQL 文本。
     *
     * <p>仅在真正输出日志的分支中调用，避免关闭全量日志后仍做无谓的字符串处理。</p>
     *
     * @param context 执行上下文
     * @return 可输出的 SQL 文本
     */
    private String sqlText(JdbcExecuteContext context) {
        return truncate(SqlTextUtils.singleLine(context.getSql()));
    }

    /**
     * 截断超长 SQL 文本，避免单条日志过大。
     *
     * @param sql 单行化后的 SQL
     * @return 截断后的 SQL
     */
    private String truncate(String sql) {
        if (sql.length() <= MAX_SQL_LOG_LENGTH) {
            return sql;
        }
        return sql.substring(0, MAX_SQL_LOG_LENGTH) + TRUNCATED_SUFFIX;
    }
}
