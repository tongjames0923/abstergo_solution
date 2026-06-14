package com.tbs.sql.logger.impl;

import com.tbs.sql.logger.ISqlLogger;
import com.tbs.sql.model.runtime.SqlRuntimeStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
