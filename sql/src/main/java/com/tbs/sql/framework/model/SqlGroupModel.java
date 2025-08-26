package com.tbs.sql.framework.model;

import java.io.Serializable;

import lombok.Data;

/**
 *
 * @author abstergo
 */
@Data
public class SqlGroupModel implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 分组字段名称
	 */
	private String fieldName;
}
