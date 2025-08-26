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
 * The type Sql logging interceptor.
 *
 * @author Abstergo
 */
@Intercepts({
		@Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
				RowBounds.class, ResultHandler.class }),
		@Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }) })
public class SqlLoggingInterceptor implements Interceptor {

	@Resource
	@Lazy
	private Map<String, ISqlLogger> sqlLoggers;

	/**
	 * Instantiates a new Sql logging interceptor.
	 */
	public SqlLoggingInterceptor() {
	}

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

	@Override
	public Object plugin(Object o) {
		return Plugin.wrap(o, this);
	}

	@Override
	public void setProperties(Properties properties) {

	}
}
