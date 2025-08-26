package com.tbs.sql.framework.model;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author abstergo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SqlSelectInfoModel implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 原始sql,若存在则优先使用
	 */
	private String rawSql;
	/**
	 * 表名
	 */
	private String tableName;
	/**
	 * 字段列表
	 */
	List<SqlSelectItemModel> fieldList;

	/**
	 * 实体类
	 */
	private Class<?> entityClass;

	public static SqlSelectInfoModel rawSql(String sql) {
		return new SqlSelectInfoModel(sql, null, null, null);
	}

	public static SqlSelectInfoModel tableName(String tableName) {
		return new SqlSelectInfoModel(null, tableName, null, null);
	}

	public static SqlSelectInfoModel custom(String tableName, List<SqlSelectItemModel> fieldList) {
		return new SqlSelectInfoModel(null, tableName, fieldList, null);
	}
}
