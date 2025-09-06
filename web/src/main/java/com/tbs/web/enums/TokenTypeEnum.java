package com.tbs.web.enums;

import com.tbs.common.BaseEnum;

import lombok.Getter;

@Getter
public enum TokenTypeEnum implements BaseEnum<String> {
	ACCESS_TOKEN("访问令牌", 1), REFRESH_TOKEN("刷新令牌", 2);

	private String value;
	private Integer code;

	TokenTypeEnum(String value, Integer code) {
		this.value = value;
		this.code = code;
	}
}
