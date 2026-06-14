package com.tbs.sql.interceptor;

import cn.hutool.core.collection.CollUtil;
import com.tbs.sql.logger.ISqlLogger;
import com.tbs.sql.model.runtime.SqlRuntimeStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.context.annotation.Lazy;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Properties;

/**
 * SQL日志MyBatis插件，用于记录SQL执行日志。
 *
 * <p>该类实现了MyBatis的 {@link Interceptor} 接口，通过拦截 {@link Executor}
 * 的query和update方法，记录SQL执行的详细日志，包括SQL语句、执行时间、结果等。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>记录SQL执行日志</li>
 *   <li>记录SQL执行时间</li>
 *   <li>记录SQL执行结果</li>
 *   <li>支持多种日志记录器 {@link ISqlLogger}</li>
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
 * // 启用SQL日志拦截器
 * // application.yml
 * tbs:
 *   sql:
 *     enable-log-interceptor: true
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>需要在配置中启用 {@code tbs.sql.enable-log-interceptor: true}</li>
 *   <li>日志记录器需要注册为Spring Bean</li>
 *   <li>日志记录失败不会中断SQL执行</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see ISqlLogger
 * @see SqlRuntimeStatus
 */
@Intercepts({
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})
@Slf4j
public class SqlLoggingInterceptor implements Interceptor {

    @Resource
    @Lazy
    private Map<String, ISqlLogger> sqlLoggers;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if (CollUtil.isEmpty(sqlLoggers)) return invocation.proceed();
        long startTime = System.currentTimeMillis();
        Object result = null;
        Throwable exception = null;
        try {
            result = invocation.proceed();
            return result;
        } catch (Throwable t) {
            exception = t;
            throw t;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            try {
                MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
                BoundSql boundSql = mappedStatement.getBoundSql(invocation.getArgs()[1]);
                String sql = boundSql.getSql();
                SqlCommandType commandType = mappedStatement.getSqlCommandType();
                SqlRuntimeStatus status = new SqlRuntimeStatus(commandType, sql, costTime, invocation.getArgs()[1], result, exception);
                for (ISqlLogger sqlLogger : sqlLoggers.values()) {
                    if (null == sqlLogger) continue;
                    sqlLogger.log(status);
                }
            } catch (Exception ignored) {
                log.warn("SQL日志记录失败...", ignored);
            }
        }
    }

    @Override
    public Object plugin(Object o) { return Plugin.wrap(o, this); }

    @Override
    public void setProperties(Properties properties) {}
}
