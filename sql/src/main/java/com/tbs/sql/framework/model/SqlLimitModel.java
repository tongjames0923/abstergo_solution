package com.tbs.sql.framework.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SQL分页模型
 * <p>
 * 封装SQL LIMIT子句中的分页信息，包括偏移量和获取数量
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SqlLimitModel implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 偏移量
	 * <p>SQL LIMIT子句中的起始位置</p>
	 */
	private Long offset;
	/**
	 * 获取数量
	 * <p>SQL LIMIT子句中要获取的记录数量</p>
	 */
	private Long count;
}
