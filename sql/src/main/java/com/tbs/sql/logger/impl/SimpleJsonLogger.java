package com.tbs.sql.logger.impl;

import com.tbs.common.util.base.JsonUtils;
import com.tbs.sql.logger.ISqlLogger;
import com.tbs.sql.model.runtime.SqlRuntimeStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 简单JSON格式SQL日志记录器实现类，以JSON格式记录SQL日志。
 *
 * <p>该类实现了 {@link ISqlLogger} 接口，以JSON格式记录SQL执行日志，
 * 包括SQL语句、执行时间、参数等信息。使用Fastjson2进行JSON序列化。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>以JSON格式记录SQL日志</li>
 *   <li>记录SQL语句、执行时间、参数</li>
 *   <li>参数安全处理，避免序列化异常</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>需要结构化日志记录时</li>
 *   <li>需要便于日志分析和查询时</li>
 *   <li>需要记录复杂参数时</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 日志输出示例
 * {
 *   "Execute SQL": "SELECT * FROM users WHERE id = ?",
 *   "cost": "15ms",
 *   "params": 123
 * }
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>需要引入Fastjson2依赖</li>
 *   <li>参数会进行安全处理，避免序列化异常</li>
 *   <li>日志格式为美化后的JSON</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see ISqlLogger
 * @see SimpleSqlLogger
 */
public class SimpleJsonLogger implements ISqlLogger {
    private static final Logger logger = LoggerFactory.getLogger(SimpleJsonLogger.class);

    @Override
    public void log(SqlRuntimeStatus sqlRuntimeStatus) {
        if (logger == null) return;
        Object params = sqlRuntimeStatus.getParameterObject();
        Object safeParams = makeParamsSafe(params);
        String logMessage = JsonUtils.toPrettyJSONString(
            Map.of("Execute SQL", sqlRuntimeStatus.getPrepareSql(), "cost",
                sqlRuntimeStatus.getExecuteTime() + "ms", "params", safeParams));
        logger.info(logMessage);
    }

    private Object makeParamsSafe(Object params) {
        if (params == null) return null;
        if (params instanceof String || params instanceof Number || params instanceof Boolean) return params;
        try { return params.toString(); } catch (Exception e) { return "UNABLE_TO_SERIALIZE_PARAMETER"; }
    }
}
