package com.tbs.app.form;

import lombok.Data;

import java.io.Serializable;

@Data
public class CreateDepartmentForm implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 部门名称
	 */
	private String name;

	/**
	 * 部门描述
	 */
	private String description;

	/**
	 * 上级部门ID
	 */
	private Long parentId;

	/**
	 * 排序
	 */
	private Integer sortOrder;

}
