package com.tbs.app.enums;

import com.tbs.common.BaseEnum;

/**
 * 系统用户状态枚举
 *
 * @author abstergo
 */
public enum SystemUserStatusEnum implements BaseEnum<String> {

    /**
     * 激活的
     */
    ACTIVITE("激活的", 1),
    /**
     * 创建的
     */
    CREATED("创建的", 2),
    /**
     * 禁用的
     */
    DISABLED("禁用的", 3);

    private String value;
    private int code;

    SystemUserStatusEnum(String value, int code) {
        this.value = value;
        this.code = code;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public int getCode() {
        return code;
    }
}
