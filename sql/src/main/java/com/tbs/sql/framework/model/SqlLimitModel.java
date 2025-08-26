package com.tbs.sql.framework.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * limit语句模型
 *
 * @author abstergo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SqlLimitModel implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 偏移量
	 */
	private Long offset;
	/**
	 * 获取数量
	 */
	private Long count;
}
