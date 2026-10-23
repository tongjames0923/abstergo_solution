package com.tbs.sql.datasource.interceptor.impl;

import com.tbs.sql.datasource.decoration.SqlTextUtils;
import com.tbs.sql.datasource.interceptor.JdbcExecutionInterceptor;
import com.tbs.sql.datasource.model.InterceptVerdict;
import com.tbs.sql.datasource.model.JdbcExecuteContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;

import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.util.List;
import java.util.Map;

/**
 * 内置 SQL 执行观测拦截器：输出 SQL、参数、执行耗时与慢 SQL 告警。
 *
 * <p>替代驱动层 p6spy 的 SQL 日志能力，观测点收敛到 JDBC 装饰层，
 * 因此日志可携带数据源标识、命令类型、批量标记与线程名。</p>
 *
 * <h3>输出策略</h3>
 * <ul>
 *   <li>执行失败：WARN，始终输出</li>
 *   <li>执行耗时达到慢 SQL 阈值：WARN（慢 SQL 告警），始终输出</li>
 *   <li>其余 SQL：INFO，由 {@code tbs.sql.log-sql} 控制是否输出</li>
 *   <li>参数值：由 {@code tbs.sql.log-parameters} 控制，批量执行按 addBatch 顺序逐组输出</li>
 * </ul>
 *
 * <h3>能力边界</h3>
 * <p>SQL 文本保留 {@code ?} 占位符，参数值由 Statement 代理采集后以列表形式追加；
 * 流式参数（InputStream / Reader / Blob / Clob）仅标记为 {@code <stream>}；
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
     * 单个参数值转义后的最大长度，超出截断，避免大字段刷爆日志
     */
    private static final int MAX_PARAM_VALUE_LENGTH = 200;

    /**
     * 是否输出全量 SQL 执行日志
     */
    private final boolean logSql;

    /**
     * 慢 SQL 告警阈值（毫秒）
     */
    private final long slowSqlThresholdMillis;

    /**
     * 是否输出 PreparedStatement 参数值
     */
    private final boolean logParameters;

    /**
     * 构造 SQL 执行观测拦截器。
     *
     * @param logSql                 是否输出全量 SQL 执行日志
     * @param slowSqlThresholdMillis 慢 SQL 告警阈值（毫秒）
     * @param logParameters          是否输出 PreparedStatement 参数值
     */
    public SqlExecutionLogInterceptor(boolean logSql, long slowSqlThresholdMillis, boolean logParameters) {
        this.logSql = logSql;
        this.slowSqlThresholdMillis = slowSqlThresholdMillis;
        this.logParameters = logParameters;
    }

    @Override
    public InterceptVerdict interceptExecution(JdbcExecuteContext context) {
        return InterceptVerdict.allow();
    }

    @Override
    public void onExecutionCompleted(JdbcExecuteContext context, long elapsedMillis, Throwable error) {
        String sql = truncate(SqlTextUtils.singleLine(context.getSql()));
        String params = describeParameters(context);
        if (error != null) {
            SQL_LOG.warn("[SQL] 执行失败: 耗时={}ms, 数据源={}, 类型={}, 批量={}, 线程={}, 异常={}, sql={}{}",
                    elapsedMillis, context.getDataSourceId(), context.getCommandKind(), context.isBatch(),
                    context.getThreadName(), error.toString(), sql, params);
            return;
        }
        if (elapsedMillis >= slowSqlThresholdMillis) {
            SQL_LOG.warn("[SQL] 慢 SQL 告警: 耗时={}ms, 阈值={}ms, 数据源={}, 类型={}, 批量={}, 线程={}, sql={}{}",
                    elapsedMillis, slowSqlThresholdMillis, context.getDataSourceId(), context.getCommandKind(),
                    context.isBatch(), context.getThreadName(), sql, params);
            return;
        }
        if (logSql) {
            SQL_LOG.info("[SQL] 执行完成: 耗时={}ms, 数据源={}, 类型={}, 批量={}, 线程={}, sql={}{}",
                    elapsedMillis, context.getDataSourceId(), context.getCommandKind(), context.isBatch(),
                    context.getThreadName(), sql, params);
        }
    }

    @Override
    public int getOrder() {
        // 观测拦截器不参与裁决顺序竞争，固定排在链尾
        return Ordered.LOWEST_PRECEDENCE;
    }

    /**
     * 拼接参数描述：非批量输出单组，批量按 addBatch 顺序以 {@code |} 分隔。
     *
     * @param context 执行上下文
     * @return 参数描述，开头带逗号分隔符；无参数或开关关闭时返回空串
     */
    private String describeParameters(JdbcExecuteContext context) {
        if (!logParameters) {
            return "";
        }
        List<Map<Integer, Object>> groups = context.getParameterGroups();
        if (groups == null || groups.isEmpty() || allEmpty(groups)) {
            return "";
        }
        StringBuilder builder = new StringBuilder(", 参数=");
        for (int i = 0; i < groups.size(); i++) {
            if (i > 0) {
                builder.append(" | ");
            }
            builder.append(formatGroup(groups.get(i)));
        }
        return truncate(builder.toString());
    }

    /**
     * 判断所有参数分组是否均为空。
     *
     * @param groups 参数分组列表
     * @return true 表示无任何参数值
     */
    private boolean allEmpty(List<Map<Integer, Object>> groups) {
        for (Map<Integer, Object> group : groups) {
            if (group != null && !group.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 格式化单组参数，按 JDBC 参数索引升序输出值。
     *
     * @param parameters 参数索引到值的映射
     * @return 形如 {@code [1, abc]} 的描述
     */
    private String formatGroup(Map<Integer, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return "[]";
        }
        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (Object value : parameters.values()) {
            if (!first) {
                builder.append(", ");
            }
            builder.append(formatValue(value));
            first = false;
        }
        return builder.append(']').toString();
    }

    /**
     * 格式化单个参数值：二进制与流式参数只输出占位描述，普通值超长截断。
     *
     * @param value 参数值
     * @return 可读的参数值描述
     */
    private String formatValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof byte[]) {
            return "byte[" + ((byte[]) value).length + "]";
        }
        if (value instanceof InputStream || value instanceof Reader
                || value instanceof Blob || value instanceof Clob) {
            return "<stream>";
        }
        String text = String.valueOf(value);
        if (text.length() <= MAX_PARAM_VALUE_LENGTH) {
            return text;
        }
        return text.substring(0, MAX_PARAM_VALUE_LENGTH) + TRUNCATED_SUFFIX;
    }

    /**
     * 截断超长文本，避免单条日志过大。
     *
     * @param text 待截断文本
     * @return 截断后的文本
     */
    private String truncate(String text) {
        if (text.length() <= MAX_SQL_LOG_LENGTH) {
            return text;
        }
        return text.substring(0, MAX_SQL_LOG_LENGTH) + TRUNCATED_SUFFIX;
    }
}
