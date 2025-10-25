package com.tbs.shared.enums;

import com.tbs.common.BaseEnum;

public enum SystemRoleFlagEnum implements BaseEnum<String> {
	/**
	 * 激活可用标志
	 */
	ACTIVE("激活", 1),
	/**
	 * 失效标志
	 */
	DISABLE("失效", 0),
	/**
	 * 系统标志，不可删除不可编辑角色
	 */
	SYSTEMED("系统", 2);

	private String value;

	private Integer code;

	SystemRoleFlagEnum(String value, Integer code) {
		this.value = value;
		this.code = code;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public Integer getCode() {
		return code;
	}
}
