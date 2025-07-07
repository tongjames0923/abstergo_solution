package com.tbs.boardcast.enums;

import com.tbs.common.BaseEnum;

/**
 * 处理模式
 */
public enum HandleMode implements BaseEnum<String> {
    SYNC("同步", 0), ASYNC("异步", 1);
    private String value;
    private int code;

    HandleMode(String value, int code) {
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
