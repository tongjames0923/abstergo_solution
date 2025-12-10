package com.tbs.sql.framework.assembler;

/**
 * SQL组装器接口
 * <p>
 * 定义SQL语句组装的方法，负责将各个SQL片段组装成完整的SQL语句
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface ISqlAssembler {
	/**
	 * 组装查询SQL语句
	 * <p>
	 * 将选择、条件、排序、分组和分页等SQL片段组装成完整的查询SQL语句
	 * </p>
	 *
	 * @param selectSql 选择SQL片段
	 * @param whereSql 条件SQL片段
	 * @param orderSql 排序SQL片段
	 * @param groupSql 分组SQL片段
	 * @param limitSql 分页SQL片段
	 * @return 完整的查询SQL语句
	 */
	public String assembleSelectSql(String selectSql, String whereSql, String orderSql, String groupSql,
			String limitSql);
}
