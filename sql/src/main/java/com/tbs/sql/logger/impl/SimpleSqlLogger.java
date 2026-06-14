package com.tbs.sql.logger.impl;

import com.tbs.sql.logger.ISqlLogger;
import com.tbs.sql.model.runtime.SqlRuntimeStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 简单SQL日志记录器实现类，以简单文本格式记录SQL日志。
 *
 * <p>该类实现了 {@link ISqlLogger} 接口，以简单的文本格式记录SQL执行日志，
 * 包括SQL语句和执行时间。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>以简单文本格式记录SQL日志</li>
 *   <li>记录SQL语句和执行时间</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>需要简单的日志记录时</li>
 *   <li>需要快速查看SQL执行情况时</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 日志输出示例
 * Execute SQL: SELECT * FROM users WHERE id = ?
 * Cost Time: 15ms
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>日志格式简单，便于快速查看</li>
 *   <li>不记录参数信息</li>
 *   <li>适用于简单的日志记录场景</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see ISqlLogger
 * @see SimpleJsonLogger
 */
public class SimpleSqlLogger implements ISqlLogger {
    private static final Logger logger = LoggerFactory.getLogger(SimpleSqlLogger.class);

    @Override
    public void log(SqlRuntimeStatus sqlRuntimeStatus) {
        logger.info("Execute SQL: " + sqlRuntimeStatus.getPrepareSql() + "\nCost Time: " + sqlRuntimeStatus.getExecuteTime() + "ms");
    }
}
