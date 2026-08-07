package com.tbs.shared.enums;

import com.tbs.common.BaseEnum;
import com.tbs.shared.constant.CommonConstant;

/**
 * 系统用户状态枚举
 *
 * @author abstergo
 */
public enum SystemUserStatusEnum implements BaseEnum<String> {

	/**
	 * 激活的
	 */
	ACTIVE("激活", CommonConstant.FLAG_ON),
	/**
	 * 被锁定的，锁定账户可登录但是无法使用需激活功能
	 */
	LOCKED("锁定", 2),
	/**
	 * 禁用的
	 */
	INACTIVE("禁用", CommonConstant.FLAG_OFF),

	/**
	 * 删除的
	 */
	DELETE("删除", -1),
	;

	private String value;
	private Integer code;

	SystemUserStatusEnum(String value, int code) {
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
