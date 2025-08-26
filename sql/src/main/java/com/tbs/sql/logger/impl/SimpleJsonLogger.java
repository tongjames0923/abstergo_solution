package com.tbs.sql.logger.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.tbs.sql.logger.ISqlLogger;
import com.tbs.sql.logger.SqlRuntimeStatus;
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

		Object params = sqlRuntimeStatus.getParameterObject();
		// 处理可能包含二进制数据的参数
		Object safeParams = makeParamsSafe(params);

		String logMessage = JSON.toJSONString(
				Map.of("Execute SQL", sqlRuntimeStatus.getPrepareSql(), "cost",
						sqlRuntimeStatus.getExecuteTime() + "ms", "params", safeParams),
				JSONWriter.Feature.PrettyFormat);
		logger.info(logMessage);
	}

	/**
	 * 确保参数可以安全地序列化为JSON
	 *
	 * @param params 原始参数
	 * @return 安全的参数
	 */
	private Object makeParamsSafe(Object params) {
		if (params == null) {
			return null;
		}

		// 如果是基本类型或字符串，直接返回
		if (params instanceof String || params instanceof Number || params instanceof Boolean) {
			return params;
		}

		// 对于其他类型，转换为字符串表示
		try {
			return params.toString();
		} catch (Exception e) {
			return "UNABLE_TO_SERIALIZE_PARAMETER";
		}
	}
}
