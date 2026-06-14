package com.tbs.sql.exception;

import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.exception.FormattedRuntimeException;

/**
 * SQL构建异常类，用于处理SQL构建过程中的异常情况。
 *
 * <p>该类继承自 {@link FormattedRuntimeException}，专门用于处理SQL构建过程中可能出现的
 * 异常情况，如SQL语法错误、参数错误、配置错误等。</p>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>SQL语法错误</li>
 *   <li>查询条件配置错误</li>
 *   <li>参数类型不匹配</li>
 *   <li>实体类配置错误</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 抛出异常
 * throw new SqlBuildException("SQL语法错误: {}", sql);
 * 
 * // 带原始异常
 * throw new SqlBuildException(cause, "SQL构建失败: {}", message);
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>异常级别为严重系统错误 {@link ErrorLevelEnum#CRITICAL_SYSTEM_ERROR}</li>
 *   <li>使用格式化消息，支持占位符</li>
 *   <li>建议在关键的SQL构建逻辑中使用此异常</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see FormattedRuntimeException
 * @see ErrorLevelEnum
 */
public class SqlBuildException extends FormattedRuntimeException {

    public SqlBuildException(String message, Object... args) {
        super(message, args);
        errorLevel(ErrorLevelEnum.CRITICAL_SYSTEM_ERROR);
    }

    public SqlBuildException(Throwable cause, String message, Object... args) {
        super(cause, message, args);
        errorLevel(ErrorLevelEnum.CRITICAL_SYSTEM_ERROR);
    }
}
