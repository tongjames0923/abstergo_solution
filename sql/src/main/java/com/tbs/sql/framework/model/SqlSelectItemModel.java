package com.tbs.sql.framework.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询字段信息
 *
 * @author abstergo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SqlSelectItemModel implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 字段名称
	 */
	private String fieldName;

	/**
	 * 字段别名
	 */
	private String fieldAlias;
}
