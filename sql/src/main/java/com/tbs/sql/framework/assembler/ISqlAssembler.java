package com.tbs.sql.framework.assembler;

/**
 * SQL组装接口
 *
 * @author abstergo
 */
public interface ISqlAssembler {
	/**
	 * 组装查询SQL
	 *
	 * @param selectSql
	 * @param whereSql
	 * @param orderSql
	 * @param groupSql
	 * @param limitSql
	 * @return
	 */
	public String assembleSelectSql(String selectSql, String whereSql, String orderSql, String groupSql,
			String limitSql);
}
