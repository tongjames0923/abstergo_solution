package com.tbs.sql.query.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.sql.framework.model.SqlSelectInfoModel;
import com.tbs.sql.query.AbstractEntityClassBaseQueryCriteria;
import com.tbs.sql.utils.EntityInfoPuller;

/**
 * 默认的基于实体类的查询条件实现
 * <p>
 * 该类通过实体类自动生成SQL查询信息，提供便捷的方式来构建基于实体的查询条件。 支持链式调用来设置实体类，并保证实体类不为空。
 */
public class DefaultEntityClassBaseQueryCriteria extends AbstractEntityClassBaseQueryCriteria {

	/**
	 * 实体类
	 */
	private Class<?> entityClass;

	/**
	 * 根据实体类获取SQL查询信息
	 *
	 * @param entity 实体类
	 * @return SqlSelectInfoModel SQL查询信息模型
	 */
	@Override
	protected SqlSelectInfoModel getSqlSelectInfoModel(Class<?> entity) {
		return EntityInfoPuller.pullFromClass(entity);
	}

	/**
	 * 构造函数，初始化实体类
	 *
	 * @param entityClass 实体类
	 */
	public DefaultEntityClassBaseQueryCriteria(Class<?> entityClass) {
		setEntityClass(entityClass);
	}

	/**
	 * 获取实体类
	 *
	 * @return 实体类
	 */
	@Override
	protected Class<?> getEntityClass() {
		return entityClass;
	}

	/**
	 * 设置实体类
	 *
	 * @param entityClass 实体类
	 * @return 当前对象，用于链式调用
	 */
	public DefaultEntityClassBaseQueryCriteria setEntityClass(Class<?> entityClass) {
		AssertUtil.notNull(entityClass, "查询实体类型不可为空。");
		this.entityClass = entityClass;
		return this;
	}
}
