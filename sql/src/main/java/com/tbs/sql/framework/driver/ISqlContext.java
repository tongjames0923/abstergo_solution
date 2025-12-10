package com.tbs.sql.framework.driver;

import java.util.Map;

import com.tbs.common.context.IContext;

/**
 * SQL上下文接口
 * <p>
 * 定义SQL查询上下文的方法，继承自IContext接口，提供获取SQL实体类、实例和参数的功能
 * </p>
 *
 * @author tbs
 * @since 1.0.0
 */
public interface ISqlContext extends IContext {

	/**
	 * 获取SQL实体类
	 * <p>
	 * 获取当前SQL查询对应的实体类
	 * </p>
	 *
	 * @return SQL实体类
	 */
	Class<?> getSqlEntityClass();

	/**
	 * 获取SQL实体类实例
	 * <p>
	 * 获取当前SQL查询对应的实体类实例
	 * </p>
	 *
	 * @return SQL实体类实例
	 */
	Object getSqlEntity();

	/**
	 * 获取SQL参数
	 * <p>
	 * 获取当前SQL查询的参数映射
	 * </p>
	 *
	 * @return SQL参数映射
	 */
	Map<String, Object> getSqlParams();
}
