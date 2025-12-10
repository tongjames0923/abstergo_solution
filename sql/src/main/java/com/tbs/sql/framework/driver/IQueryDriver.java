package com.tbs.sql.framework.driver;

import java.util.List;

import com.tbs.sql.framework.model.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询驱动接口
 * <p>
 * 定义获取SQL查询数据的方法，用于生成SQL查询语句
 * </p>
 *
 * @author tbs
 * @since 1.0.0
 */
public interface IQueryDriver {
	/**
	 * SQL查询数据类
	 * <p>
	 * 封装SQL查询所需的各种信息，包括查询上下文、选择信息、分组信息、分页信息、排序信息和条件信息
	 * </p>
	 */
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static final class SqlQueryData {
		/**
		 * SQL查询上下文
		 */
		private ISqlContext context;
		/**
		 * SQL选择信息
		 */
		private SqlSelectInfoModel selectInfo;
		/**
		 * SQL分组信息列表
		 */
		private List<SqlGroupModel> sqlGroups;
		/**
		 * SQL分页信息
		 */
		private SqlLimitModel sqlLimit;
		/**
		 * SQL排序信息列表
		 */
		private List<SqlOrderInfo> sqlOrders;
		/**
		 * SQL条件信息
		 */
		private SqlWhereCompose sqlWhereInfo;
	}

	/**
	 * 获取SQL查询数据
	 * <p>
	 * 根据查询上下文生成SQL查询所需的各种信息
	 * </p>
	 *
	 * @param context SQL查询上下文
	 * @return SQL查询数据
	 */
	public SqlQueryData getSqlQueryData(ISqlContext context);

}
