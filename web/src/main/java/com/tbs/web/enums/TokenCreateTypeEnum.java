package com.tbs.web.enums;

import com.tbs.common.BaseEnum;

import lombok.Getter;

/**
 * 创建token方式枚举
 */
@Getter
public enum TokenCreateTypeEnum implements BaseEnum<String> {
	LOGIN("登录", 1), REFRESH("刷新替换", 2), THIRD("第三方登录", 3);

	private String value;
	private Integer code;

	private TokenCreateTypeEnum(String value, Integer code) {
		this.value = value;
		this.code = code;
	}
}
