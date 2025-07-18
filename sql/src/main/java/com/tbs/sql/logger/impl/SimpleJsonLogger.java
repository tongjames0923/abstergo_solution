package com.tbs.sql.logger.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.tbs.sql.logger.ISqlLogger;
import com.tbs.sql.model.SqlRuntimeStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author abstergo
 */
public class SimpleJsonLogger implements ISqlLogger {

    public SimpleJsonLogger() {
    }

    private static final Logger logger = LoggerFactory.getLogger(SimpleJsonLogger.class);

    @Override
    public void log(SqlRuntimeStatus sqlRuntimeStatus) {
        if (logger == null) {
            return;
        }
        String logMessage = JSON.toJSONString(
            Map.of("Execute SQL", sqlRuntimeStatus.getPrepareSql(), "cost", sqlRuntimeStatus.getExecuteTime() + "ms",
                "params", sqlRuntimeStatus.getParameterObject()), JSONWriter.Feature.PrettyFormat);
        logger.info(logMessage);
    }
}
