package com.tbs.web.enums;

import com.tbs.common.BaseEnum;

import lombok.Getter;

@Getter
public enum TokenTypeEnum implements BaseEnum<String> {
	WEB_ACCESS_TOKEN("访问令牌", 1), REFRESH_TOKEN("刷新令牌", 2), APPLICATION_ACCESS_TOKEN("应用访问令牌", 3);

	private String value;
	private Integer code;

	TokenTypeEnum(String value, Integer code) {
		this.value = value;
		this.code = code;
	}
}
