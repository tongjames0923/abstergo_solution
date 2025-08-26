package com.tbs.sql.framework.driver.impl;

import java.util.List;

import com.tbs.common.error.AssertUtil;
import com.tbs.sql.framework.driver.IQueryDriver;
import com.tbs.sql.framework.driver.ISqlContext;
import com.tbs.sql.framework.model.*;

/**
 * SQL驱动抽象类，实现了IQueryDriver接口 提供了SQL查询相关的基本框架和抽象方法，具体实现由子类完成
 */
public abstract class AbstractSqlDriver implements IQueryDriver {

	/**
	 * 获取SQL查询的SELECT信息
	 *
	 * @param context SQL上下文环境
	 * @return SqlSelectInfoModel SELECT信息模型
	 */
	protected abstract SqlSelectInfoModel getSelectInfo(ISqlContext context);

	/**
	 * 获取SQL查询的WHERE条件信息
	 *
	 * @param context SQL上下文环境
	 * @return SqlWhereCompose WHERE条件组合
	 */
	protected abstract SqlWhereCompose getSqlWhereInfo(ISqlContext context);

	/**
	 * 获取SQL查询的GROUP BY信息
	 *
	 * @param context SQL上下文环境
	 * @return List<SqlGroupModel> 分组模型列表
	 */
	protected abstract List<SqlGroupModel> getSqlGroups(ISqlContext context);

	/**
	 * 获取SQL查询的LIMIT信息
	 *
	 * @param context SQL上下文环境
	 * @return SqlLimitModel LIMIT模型
	 */
	protected abstract SqlLimitModel getSqlLimit(ISqlContext context);

	/**
	 * 获取SQL查询的ORDER BY信息
	 *
	 * @param context SQL上下文环境
	 * @return List<SqlOrderInfo> 排序信息列表
	 */
	protected abstract List<SqlOrderInfo> getSqlOrders(ISqlContext context);

	/**
	 * 判断是否支持指定的上下文环境
	 *
	 * @param context SQL上下文环境
	 * @return boolean 是否支持该上下文，默认返回true
	 */
	protected boolean supportContext(ISqlContext context) {
		return true;
	}

	/**
	 * 获取SQL查询数据对象 整合所有SQL查询相关的信息，包括SELECT、WHERE、GROUP BY、LIMIT、ORDER BY等
	 *
	 * @param context SQL上下文环境
	 * @return SqlQueryData SQL查询数据对象
	 */
	@Override
	public SqlQueryData getSqlQueryData(ISqlContext context) {
		AssertUtil.notNull(context, "环境上下文不能为空");
		AssertUtil.isTrue(!supportContext(context), "不支持该环境上下文{0}", context);
		AssertUtil.isTrue(!context.isValid(), "环境上下文{0}已失效", context);
		SqlSelectInfoModel selectInfo = getSelectInfo(context);
		SqlWhereCompose whereCompose = getSqlWhereInfo(context);
		List<SqlGroupModel> groupModels = getSqlGroups(context);
		SqlLimitModel limitModel = getSqlLimit(context);
		List<SqlOrderInfo> orderModels = getSqlOrders(context);
		return new SqlQueryData(context, selectInfo, groupModels, limitModel, orderModels, whereCompose);
	}
}
