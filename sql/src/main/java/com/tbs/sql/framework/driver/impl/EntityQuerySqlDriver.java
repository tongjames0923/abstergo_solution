package com.tbs.sql.framework.driver.impl;

import java.util.List;

import com.tbs.common.util.base.BeanUtil;
import com.tbs.sql.framework.driver.ISqlContext;
import com.tbs.sql.framework.model.*;
import com.tbs.sql.query.IQueryCriteria;
import com.tbs.sql.utils.EntityInfoPuller;

import lombok.Setter;

public class EntityQuerySqlDriver extends AbstractSqlDriver {

	@Setter
	List<SqlGroupModel> sqlGroups;

	@Override
	protected SqlSelectInfoModel getSelectInfo(ISqlContext context) {
		SqlSelectInfoModel selectInfoModel;
		if (BeanUtil.isBaseFrom(context.getSqlEntityClass(), IQueryCriteria.class)) {
			selectInfoModel = ((IQueryCriteria) context.getSqlEntity()).getSelectSQLInfo();
		} else {
			selectInfoModel = EntityInfoPuller.pullFromClass(context.getSqlEntityClass());
		}
		selectInfoModel.setEntityClass(context.getSqlEntityClass());
		return selectInfoModel;
	}

	private SqlWhereCompose compose;

	private SqlLimitModel limit;

	private List<SqlOrderInfo> orders;

	public EntityQuerySqlDriver setWhereInfo(SqlWhereCompose compose) {
		this.compose = compose;
		return this;
	}

	public EntityQuerySqlDriver setLimit(SqlLimitModel limit) {
		this.limit = limit;
		return this;
	}

	public EntityQuerySqlDriver setOrders(List<SqlOrderInfo> orders) {
		this.orders = orders;
		return this;
	}

	@Override
	protected SqlWhereCompose getSqlWhereInfo(ISqlContext context) {
		return compose;
	}

	@Override
	protected List<SqlGroupModel> getSqlGroups(ISqlContext context) {
		return this.sqlGroups;
	}

	@Override
	protected SqlLimitModel getSqlLimit(ISqlContext context) {
		return limit;
	}

	@Override
	protected List<SqlOrderInfo> getSqlOrders(ISqlContext context) {
		return orders;
	}
}
