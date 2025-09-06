package com.tbs.web.enums;

import com.tbs.common.BaseEnum;

import lombok.Getter;

/**
 * 创建token方式枚举
 */
@Getter
public enum AccessTokenCreateTypeEnum implements BaseEnum<String> {
	LOGIN("登录", 1), REFRESH("刷新替换", 2);

	private String value;
	private Integer code;

	private AccessTokenCreateTypeEnum(String value, Integer code) {
		this.value = value;
		this.code = code;
	}
}
