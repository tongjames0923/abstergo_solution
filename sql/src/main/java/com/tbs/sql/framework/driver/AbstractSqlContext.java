package com.tbs.sql.framework.driver;

import java.util.HashMap;
import java.util.Map;

/**
 * sql上下文抽象类
 *
 * @author tbs
 */
public abstract class AbstractSqlContext implements ISqlContext {
	private Map<String, Object> sqlParams = new HashMap<>(4);
	private boolean valid = true;

	private Object sqlEntity;

    private Class<?> sqlEntityClass;

	@Override
	public Class<?> getSqlEntityClass() {
        return sqlEntityClass;
	}

	/**
	 * 获取sql实体
	 *
	 * @return
	 */
	protected abstract Object provideSqlEntity();

	/**
	 * 清空sql实体
	 */
	protected void clearEntity() {
		sqlEntity = null;
        sqlEntityClass = null;
	}

	@Override
	public Object getSqlEntity() {
		return sqlEntity;
	}

	@Override
	public Map<String, Object> getSqlParams() {
		return sqlParams;
	}

	@Override
	public void destroy() {
		sqlParams.clear();
		sqlEntity = null;
        sqlEntityClass = null;
		valid = false;
	}

	@Override
	public void rebuild() {
		valid = true;
		sqlParams.clear();
		sqlEntity = provideSqlEntity();
        sqlEntityClass = sqlEntity.getClass();
	}

	@Override
	public boolean isValid() {
		return valid;
	}
}
