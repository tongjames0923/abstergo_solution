package com.tbs.sql.framework.intercepter;

import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.context.annotation.Lazy;

import com.tbs.sql.logger.ISqlLogger;
import com.tbs.sql.logger.SqlRuntimeStatus;

import cn.hutool.core.collection.CollUtil;

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
			Object result = invocation.proceed();
			return result;
		}
		Date date = new Date();
		Object result = invocation.proceed();
		Date date1 = new Date();
		long costTime = date1.getTime() - date.getTime();
		MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
		BoundSql boundSql = mappedStatement.getBoundSql(invocation.getArgs()[1]);
		String sql = boundSql.getSql();
		SqlCommandType commandType = mappedStatement.getSqlCommandType();
		SqlRuntimeStatus status = new SqlRuntimeStatus(commandType, sql, costTime, invocation.getArgs()[1], result);
		for (ISqlLogger sqlLogger : sqlLoggers.values()) {
			if (null == sqlLogger) {
				continue;
			}
			sqlLogger.log(status);
		}

		return result;
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
