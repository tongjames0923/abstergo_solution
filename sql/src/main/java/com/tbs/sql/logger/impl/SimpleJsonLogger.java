package com.tbs.sql.logger.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.tbs.sql.logger.ISqlLogger;
import com.tbs.sql.model.runtime.SqlRuntimeStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SimpleJsonLogger implements ISqlLogger {
    private static final Logger logger = LoggerFactory.getLogger(SimpleJsonLogger.class);

    @Override
    public void log(SqlRuntimeStatus sqlRuntimeStatus) {
        if (logger == null) return;
        Object params = sqlRuntimeStatus.getParameterObject();
        Object safeParams = makeParamsSafe(params);
        String logMessage = JSON.toJSONString(
            Map.of("Execute SQL", sqlRuntimeStatus.getPrepareSql(), "cost",
                sqlRuntimeStatus.getExecuteTime() + "ms", "params", safeParams),
            JSONWriter.Feature.PrettyFormat);
        logger.info(logMessage);
    }

    private Object makeParamsSafe(Object params) {
        if (params == null) return null;
        if (params instanceof String || params instanceof Number || params instanceof Boolean) return params;
        try { return params.toString(); } catch (Exception e) { return "UNABLE_TO_SERIALIZE_PARAMETER"; }
    }
}
