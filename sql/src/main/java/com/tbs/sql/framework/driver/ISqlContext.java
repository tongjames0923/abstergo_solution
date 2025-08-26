package com.tbs.sql.framework.driver;

import java.util.Map;

import com.tbs.common.context.IContext;

public interface ISqlContext extends IContext {

	/**
	 * 获取SQL实体类
	 *
	 * @return
	 */
	Class<?> getSqlEntityClass();

	/**
	 * 获取SQL实体类实例
	 *
	 * @return
	 */
	Object getSqlEntity();

	/**
	 * 获取SQL参数
	 *
	 * @return
	 */
	Map<String, Object> getSqlParams();
}
