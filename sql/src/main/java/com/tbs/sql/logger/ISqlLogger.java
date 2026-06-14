package com.tbs.sql.logger;

import com.tbs.sql.model.runtime.SqlRuntimeStatus;
import org.jetbrains.annotations.NotNull;

/**
 * SQL运行时日志记录器接口，用于记录SQL执行过程中的日志信息。
 *
 * <p>该接口定义了SQL日志记录的标准方法，允许在SQL执行过程中记录详细的日志信息，
 * 包括SQL语句、执行时间、参数、结果等。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>记录SQL执行日志 {@link #log(SqlRuntimeStatus)}</li>
 *   <li>支持自定义日志记录逻辑</li>
 *   <li>支持多种日志格式</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>记录SQL执行日志用于调试</li>
 *   <li>监控SQL执行性能</li>
 *   <li>记录SQL执行异常</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 实现日志记录器
 * @Component
 * public class CustomSqlLogger implements ISqlLogger {
 *     @Override
 *     public void log(SqlRuntimeStatus status) {
 *         System.out.println("SQL: " + status.getPrepareSql());
 *         System.out.println("Time: " + status.getExecuteTime() + "ms");
 *     }
 * }
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>实现类需要注册为Spring Bean</li>
 *   <li>日志记录不应该影响SQL执行性能</li>
 *   <li>确保日志记录线程安全</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see SqlRuntimeStatus
 * @see com.tbs.sql.interceptor.SqlLoggingInterceptor
 */
public interface ISqlLogger {
    /**
     * 记录SQL执行日志。
     *
     * @param sqlRuntimeStatus SQL运行时状态，包含SQL语句、执行时间、参数、结果等信息
     */
    void log(@NotNull SqlRuntimeStatus sqlRuntimeStatus);
}
