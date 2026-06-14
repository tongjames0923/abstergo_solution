package com.tbs.sql.logger.impl;

import com.tbs.sql.logger.ISqlLogger;
import com.tbs.sql.model.runtime.SqlRuntimeStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleSqlLogger implements ISqlLogger {
    private static final Logger logger = LoggerFactory.getLogger(SimpleSqlLogger.class);

    @Override
    public void log(SqlRuntimeStatus sqlRuntimeStatus) {
        logger.info("Execute SQL: " + sqlRuntimeStatus.getPrepareSql() + "\nCost Time: " + sqlRuntimeStatus.getExecuteTime() + "ms");
    }
}
