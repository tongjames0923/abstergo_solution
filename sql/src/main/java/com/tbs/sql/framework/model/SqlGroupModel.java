package com.tbs.sql.framework.model;

import java.io.Serializable;

import lombok.Data;

/**
 * SQL分组模型
 * <p>
 * 封装SQL GROUP BY子句中的分组字段信息
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class SqlGroupModel implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 分组字段名称
	 * <p>SQL GROUP BY子句中的字段名称</p>
	 */
	private String fieldName;
}
