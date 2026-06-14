package com.tbs.sql.logger.impl;

import com.tbs.sql.logger.ISqlLogger;
import com.tbs.sql.model.runtime.SqlRuntimeStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 空SQL日志记录器实现类，不记录任何SQL日志。
 *
 * <p>该类实现了 {@link ISqlLogger} 接口，但不执行任何实际的日志记录操作。
 * 仅在首次调用时输出一条警告日志，用于提示当前使用的是空日志记录器。</p>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>需要禁用SQL日志记录时</li>
 *   <li>作为默认的日志记录器使用</li>
 *   <li>性能敏感场景下避免日志开销</li>
 * </ul>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>首次调用会输出警告日志</li>
 *   <li>后续调用不会执行任何操作</li>
 *   <li>适用于不需要SQL日志的场景</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see ISqlLogger
 */
public class NoSqlLogger implements ISqlLogger {
    private boolean hasShow = false;
    private static final Logger logger = LoggerFactory.getLogger(NoSqlLogger.class);

    @Override
    public void log(SqlRuntimeStatus sqlRuntimeStatus) {
        if (!hasShow) {
            hasShow = true;
            logger.warn("NoSqlLogger is running");
        }
    }
}
