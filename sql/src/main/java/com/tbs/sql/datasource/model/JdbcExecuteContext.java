package com.tbs.sql.datasource.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * JDBC SQL 执行阶段的拦截上下文。
 *
 * <p>由 Statement 代理在 execute 系方法调用前构造，传递给
 * {@link com.tbs.sql.datasource.interceptor.JdbcExecutionInterceptor#interceptExecution}；
 * 执行结束后携带参数分组再次传递，供
 * {@link com.tbs.sql.datasource.interceptor.JdbcExecutionInterceptor#onExecutionCompleted} 观测。</p>
 *
 * <h3>能力边界</h3>
 * <ul>
 *   <li>SQL 文本为最终执行语句（含 {@code ?} 占位符），仅供只读裁决使用，框架<b>不提供</b>改写能力</li>
 *   <li>参数分组采集自 PreparedStatement 的 {@code setXxx} 调用，仅供观测使用；
 *       裁决阶段该列表为空，不感知参数值</li>
 *   <li>批量执行时按批次内逐条 SQL 分别构造裁决上下文，观测阶段按 addBatch 顺序逐组携带参数</li>
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

    /**
     * PreparedStatement 参数分组：组内 key 为 JDBC 参数索引（从 1 开始），value 为 setXxx 设置的值。
     * 非批量执行只含一组；批量执行按 addBatch 顺序逐组排列；裁决阶段为空列表。
     */
    private List<Map<Integer, Object>> parameterGroups;

    /**
     * 兼容构造器：不含参数分组的执行上下文，供裁决阶段使用。
     *
     * @param sql         待执行的 SQL 文本
     * @param commandKind 推断出的命令类型
     * @param dataSourceId 数据源标识
     * @param threadName  执行 SQL 的线程名
     * @param batch       是否为批量执行
     */
    public JdbcExecuteContext(String sql, SqlCommandKind commandKind, String dataSourceId,
                              String threadName, boolean batch) {
        this(sql, commandKind, dataSourceId, threadName, batch, Collections.emptyList());
    }
}
