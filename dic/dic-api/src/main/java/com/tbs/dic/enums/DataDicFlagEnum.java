package com.tbs.dic.enums;

import com.tbs.common.BaseEnum;

public enum DataDicFlagEnum implements BaseEnum<String> {
    DISABLE("停用", -1), ENABLE("启用", 1), DELETE("删除", 0);

    private String val;
    private Integer code;

    DataDicFlagEnum(String val, Integer code) {
        this.val = val;
        this.code = code;
    }

    @Override
    public String getValue() {
        return val;
    }

    @Override
    public Integer getCode() {
        return code;
    }
}
