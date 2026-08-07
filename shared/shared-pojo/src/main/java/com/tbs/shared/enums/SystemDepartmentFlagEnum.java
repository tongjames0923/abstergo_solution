package com.tbs.shared.enums;

import com.tbs.common.BaseEnum;

/**
 * 系统部门状态枚举类 用于表示部门实体的各种状态，包括删除、激活、禁用和系统内置状态
 */
public enum SystemDepartmentFlagEnum implements BaseEnum<String> {

	/** 已激活状态 */
	ACTIVITED("已激活", 1),
	/** 禁用状态 */
	DISABLED("禁用", 2), DELETED("已删除", 0);

	private String val;

	private Integer code;

	/**
	 * 构造函数
	 *
	 * @param val  状态描述值
	 * @param code 状态代码
	 */
	SystemDepartmentFlagEnum(String val, Integer code) {
		this.val = val;
		this.code = code;
	}

	/**
	 * 获取状态描述值
	 *
	 * @return 状态描述字符串
	 */
	@Override
	public String getValue() {
		return val;
	}

	/**
	 * 获取状态代码
	 *
	 * @return 状态代码
	 */
	@Override
	public Integer getCode() {
		return code;
	}
}
