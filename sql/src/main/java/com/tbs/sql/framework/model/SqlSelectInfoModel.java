package com.tbs.sql.framework.model;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SQL查询信息模型
 * <p>
 * 封装SQL查询的各种信息，包括原始SQL、表名、字段列表和实体类
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SqlSelectInfoModel implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 原始SQL语句
	 * <p>如果存在，则优先使用该SQL语句</p>
	 */
	private String rawSql;
	/**
	 * 表名
	 * <p>SQL查询的目标表名</p>
	 */
	private String tableName;
	/**
	 * 字段列表
	 * <p>SQL查询的字段列表</p>
	 */
	List<SqlSelectItemModel> fieldList;

	/**
	 * 实体类
	 * <p>SQL查询结果对应的实体类</p>
	 */
	private Class<?> entityClass;

	/**
	 * 创建原始SQL查询信息模型
	 *
	 * @param sql 原始SQL语句
	 * @return SQL查询信息模型
	 */
	public static SqlSelectInfoModel rawSql(String sql) {
		return new SqlSelectInfoModel(sql, null, null, null);
	}

	/**
	 * 创建表名查询信息模型
	 *
	 * @param tableName 表名
	 * @return SQL查询信息模型
	 */
	public static SqlSelectInfoModel tableName(String tableName) {
		return new SqlSelectInfoModel(null, tableName, null, null);
	}

	/**
	 * 创建自定义查询信息模型
	 *
	 * @param tableName 表名
	 * @param fieldList 字段列表
	 * @return SQL查询信息模型
	 */
	public static SqlSelectInfoModel custom(String tableName, List<SqlSelectItemModel> fieldList) {
		return new SqlSelectInfoModel(null, tableName, fieldList, null);
	}
}
