package com.tbs.app.enums;

import com.tbs.app.constant.CommonConstant;
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
    ACTIVITE("激活的", CommonConstant.FLAG_ON),
    /**
     * 被锁定的，锁定账户可登录但是无法使用需激活功能
     */
    LOCKED("被锁定的", 2),
    /**
     * 禁用的
     */
    DISABLED("禁用的", CommonConstant.FLAG_OFF);

    private String value;
    private Integer code;

    SystemUserStatusEnum(String value, int code) {
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
