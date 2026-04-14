package com.tbs.shared.enums;

import com.tbs.common.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 动态表单配置标识枚举
 */
@Getter
@AllArgsConstructor
public enum DynamicFormConfigFlagEnum implements BaseEnum<String> {

    ACTIVITED("已启用", 1), INACTIVITED("未启用", 0), DELETED("已删除", -1);

    private String value;
    private Integer code;
}
