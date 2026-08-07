package com.tbs.web.enums;

import com.tbs.common.BaseEnum;

/**
 * 访问令牌标识枚举
 * @author abstergo
 */
public enum TokenStatusEnum implements BaseEnum<String> {

    /**
     * 生效中
     */
    ACTIVE("有效", 1),
    /**
     * 已删除
     */
    DELETED("已删除", 0),
    /**
     * 无效
     */
    INACTIVE("无效", -3),

    /**
     * 已撤销
     */
    REVOKE("已撤销", -1),
    /**
     * 已过期
     */
    EXPIRED("已过期", -2);

    private String value;
    private Integer code;

    TokenStatusEnum(String value, Integer code) {
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
