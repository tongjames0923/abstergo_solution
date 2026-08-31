package com.tbs.web.enums;

import com.tbs.common.BaseEnum;

/**
 * 令牌状态枚举
 * <p>
 * 描述令牌在其生命周期中的各种状态：生效、已删除、无效、已撤销、已过期。
 * 校验与吊销流程依据此状态判定令牌是否可用（如 {@code ACTIVE} 才视为有效）。
 * </p>
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
