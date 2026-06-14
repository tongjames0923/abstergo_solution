package com.tbs.sql.logger;

import com.tbs.sql.model.runtime.SqlRuntimeStatus;
import org.jetbrains.annotations.NotNull;

/**
 * SQL运行时日志记录器
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface ISqlLogger {
    void log(@NotNull SqlRuntimeStatus sqlRuntimeStatus);
}
