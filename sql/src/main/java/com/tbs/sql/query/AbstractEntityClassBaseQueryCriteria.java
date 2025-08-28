package com.tbs.sql.query;

import com.tbs.common.error.AssertUtil;
import com.tbs.sql.framework.model.SqlSelectInfoModel;

/**
 * 抽象实体查询条件，基于实体自动产生SqlSlectInfoModel
 */
public abstract class AbstractEntityClassBaseQueryCriteria implements IQueryCriteria {

	/**
	 * 根据实体类获取SqlSelectInfoModel
	 *
	 * @param entity 实体类
	 * @return SqlSelectInfoModel
	 */
	protected abstract SqlSelectInfoModel getSqlSelectInfoModel(Class<?> entity);

	/**
	 * 获取实体类
	 *
	 * @return 实体类
	 */
	protected abstract Class<?> getEntityClass();

	@Override
	public SqlSelectInfoModel getSelectSQLInfo() {
		Class<?> entityClass = getEntityClass();
		AssertUtil.notNull(entityClass, "查询实体的类型不可为空。");
		SqlSelectInfoModel sqlSelectInfoModel = getSqlSelectInfoModel(entityClass);
		AssertUtil.notNull(sqlSelectInfoModel, "获取抽象查询数据为空。");
		return sqlSelectInfoModel;
	}
}
