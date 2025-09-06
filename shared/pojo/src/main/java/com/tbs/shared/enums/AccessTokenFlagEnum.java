package com.tbs.shared.enums;

import com.tbs.common.BaseEnum;

/**
 * 访问令牌标识枚举
 */
public enum AccessTokenFlagEnum implements BaseEnum<String> {

	ACTIVE("有效", 1), INACTIVE("无效", 0), REVOKE("已撤销", -1), EXPIRED("已过期", -2);

	private String value;
	private Integer code;

	AccessTokenFlagEnum(String value, Integer code) {
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
