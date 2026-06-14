package com.tbs.sql.framework.intercepter;

import cn.hutool.core.collection.CollUtil;
import com.tbs.sql.logger.ISqlLogger;
import com.tbs.sql.logger.SqlRuntimeStatus;
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
 * SQL日志拦截器
 * <p>
 * MyBatis插件，用于拦截SQL执行，记录SQL日志信息
 * </p>
 *
 * @author Abstergo
 * @since 1.0.0
 */
@Intercepts({
		@Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
				RowBounds.class, ResultHandler.class }),
		@Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }) })
@Slf4j
public class SqlLoggingInterceptor implements Interceptor {

	/**
	 * SQL日志记录器映射
	 */
	@Resource
	@Lazy
	private Map<String, ISqlLogger> sqlLoggers;

	/**
	 * 构造函数
	 */
	public SqlLoggingInterceptor() {
	}

	/**
	 * 拦截SQL执行，记录SQL日志
	 *
	 * @param invocation MyBatis插件调用对象
	 * @return 原始操作的结果
	 * @throws Throwable 如果发生异常
	 */
	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		if (CollUtil.isEmpty(sqlLoggers)) {
			return invocation.proceed();
		}
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
				MappedStatement mappedStatement = (MappedStatement)invocation.getArgs()[0];
				BoundSql boundSql = mappedStatement.getBoundSql(invocation.getArgs()[1]);
				String sql = boundSql.getSql();
				SqlCommandType commandType = mappedStatement.getSqlCommandType();
				SqlRuntimeStatus status =
					new SqlRuntimeStatus(commandType, sql, costTime, invocation.getArgs()[1], result, exception);
				for (ISqlLogger sqlLogger : sqlLoggers.values()) {
					if (null == sqlLogger) {
						continue;
					}
					sqlLogger.log(status);
				}
			} catch (Exception ignored) {
				log.warn("SQL日志记录失败...", ignored);
				// 日志记录失败不应影响SQL执行流程
			}
		}
	}

	/**
	 * 创建代理对象，用于拦截目标对象
	 *
	 * @param o 目标对象
	 * @return 代理对象
	 */
	@Override
	public Object plugin(Object o) {
		return Plugin.wrap(o, this);
	}

	/**
	 * 设置插件属性
	 *
	 * @param properties 插件属性
	 */
	@Override
	public void setProperties(Properties properties) {

	}
}
