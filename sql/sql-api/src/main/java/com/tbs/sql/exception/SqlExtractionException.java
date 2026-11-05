package com.tbs.sql.exception;

import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.exception.FormattedRuntimeException;

/**
 * SQL提取异常类，用于处理SQL提取过程中的异常情况。
 *
 * <p>该类继承自 {@link FormattedRuntimeException}，专门用于处理SQL提取过程中可能出现的
 * 异常情况，如提取器配置错误、数据格式错误等。</p>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>SQL提取器配置错误</li>
 *   <li>数据格式错误</li>
 *   <li>提取逻辑错误</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 抛出异常
 * throw new SqlExtractionException("提取失败: {}", source, message);
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>异常级别为严重系统错误 {@link ErrorLevelEnum#CRITICAL_SYSTEM_ERROR}</li>
 *   <li>异常消息包含来源信息，便于调试</li>
 *   <li>建议在SQL提取逻辑中使用此异常</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see FormattedRuntimeException
 * @see ErrorLevelEnum
 */
public class SqlExtractionException extends FormattedRuntimeException {

    public SqlExtractionException(String message, Object from, Object... args) {
        super("from:{} ,details:" + message, from, args);
        errorLevel(ErrorLevelEnum.CRITICAL_SYSTEM_ERROR);
    }
}
