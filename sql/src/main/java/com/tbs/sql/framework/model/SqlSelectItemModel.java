package com.tbs.sql.framework.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SQL查询字段模型
 * <p>
 * 封装SQL查询中的字段信息，包括字段名称和字段别名
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
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
