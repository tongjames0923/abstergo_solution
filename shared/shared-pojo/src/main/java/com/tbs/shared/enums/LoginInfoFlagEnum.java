package com.tbs.shared.enums;

import com.tbs.common.BaseEnum;
import com.tbs.shared.constant.CommonConstant;

public enum LoginInfoFlagEnum implements BaseEnum<String> {
    /**
     * 逻辑删除的标记
     */
    DELETED("已删除", CommonConstant.FLAG_OFF),
    /**
     * 正常的标记
     */
    ACTIVE("正常", CommonConstant.FLAG_ON),
    /**
     * 取消的标记，被撤销有效性的标记
     */
    CANCELLED("已撤销", -1);

    private String value;

    private Integer code;

    LoginInfoFlagEnum(String value, Integer code) {
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
