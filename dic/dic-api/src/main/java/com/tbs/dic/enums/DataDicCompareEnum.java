package com.tbs.dic.enums;

import com.tbs.common.BaseEnum;

public enum DataDicCompareEnum implements BaseEnum<String> {
    EQUALS("相等", 1), NOT_EQUALS("不相等", 2), NO_DIC("字典编号不存在", -1), NO_ITEM("字典项不存在", 0);
    private String val;
    private Integer code;

    DataDicCompareEnum(String val, Integer code) {
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
