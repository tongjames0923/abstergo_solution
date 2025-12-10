package com.tbs.sql.framework.driver;

import java.util.HashMap;
import java.util.Map;

/**
 * SQL上下文抽象类
 * <p>
 * 实现ISqlContext接口，提供SQL查询上下文的基本实现
 * </p>
 *
 * @author tbs
 * @since 1.0.0
 */
public abstract class AbstractSqlContext implements ISqlContext {
	/**
	 * SQL参数映射
	 */
	private Map<String, Object> sqlParams = new HashMap<>(4);
	/**
	 * 上下文是否有效
	 */
	private boolean valid = true;

	/**
	 * SQL实体实例
	 */
	private Object sqlEntity;

    /**
     * SQL实体类
     */
    private Class<?> sqlEntityClass;

	/**
	 * 获取SQL实体类
	 * <p>
	 * 获取当前SQL查询对应的实体类
	 * </p>
	 *
	 * @return SQL实体类
	 */
	@Override
	public Class<?> getSqlEntityClass() {
        return sqlEntityClass;
	}

	/**
	 * 提供SQL实体实例
	 * <p>
	 * 抽象方法，由子类实现，用于提供SQL实体实例
	 * </p>
	 *
	 * @return SQL实体实例
	 */
	protected abstract Object provideSqlEntity();

	/**
	 * 清空SQL实体
	 * <p>
	 * 清空SQL实体实例和实体类
	 * </p>
	 */
	protected void clearEntity() {
		sqlEntity = null;
        sqlEntityClass = null;
	}

	/**
	 * 获取SQL实体实例
	 * <p>
	 * 获取当前SQL查询对应的实体类实例
	 * </p>
	 *
	 * @return SQL实体实例
	 */
	@Override
	public Object getSqlEntity() {
		return sqlEntity;
	}

	/**
	 * 获取SQL参数
	 * <p>
	 * 获取当前SQL查询的参数映射
	 * </p>
	 *
	 * @return SQL参数映射
	 */
	@Override
	public Map<String, Object> getSqlParams() {
		return sqlParams;
	}

	/**
	 * 销毁上下文
	 * <p>
	 * 清空SQL参数映射，重置SQL实体实例和实体类，将上下文标记为无效
	 * </p>
	 */
	@Override
	public void destroy() {
		sqlParams.clear();
		sqlEntity = null;
        sqlEntityClass = null;
		valid = false;
	}

	/**
	 * 重建上下文
	 * <p>
	 * 将上下文标记为有效，清空SQL参数映射，重新获取SQL实体实例和实体类
	 * </p>
	 */
	@Override
	public void rebuild() {
		valid = true;
		sqlParams.clear();
		sqlEntity = provideSqlEntity();
        sqlEntityClass = sqlEntity.getClass();
	}

	/**
	 * 检查上下文是否有效
	 * <p>
	 * 返回上下文的有效性状态
	 * </p>
	 *
	 * @return 上下文是否有效
	 */
	@Override
	public boolean isValid() {
		return valid;
	}
}
