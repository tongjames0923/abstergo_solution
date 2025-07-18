package com.tbs.sql.logger;

import com.tbs.sql.model.SqlRuntimeStatus;
import org.jetbrains.annotations.NotNull;


/**
 * mybatis sql 运行时日志记录器
 *
 * @author Abstergo
 */
public interface ISqlLogger {
    /**
     * Log.
     *
     * @param sqlRuntimeStatus the sql runtime status
     */
    void log(@NotNull SqlRuntimeStatus sqlRuntimeStatus);

}
