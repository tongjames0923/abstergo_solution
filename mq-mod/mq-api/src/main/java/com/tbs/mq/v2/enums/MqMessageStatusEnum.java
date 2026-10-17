package com.tbs.mq.v2.enums;

import com.tbs.common.BaseEnum;

public enum MqMessageStatusEnum implements BaseEnum<String> {
    RECEIVED(0, "received"), IN_MEMORY(1, "in_memory"), PERSISTED(2, "persisted"), PROCESSING(3, "processing"),
    PROCESSED(4, "processed"), FAILED(5, "failed");

    private Integer code;
    private String value;

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getValue() {
        return value;
    }

    MqMessageStatusEnum(Integer code, String value) {
        this.code = code;
        this.value = value;
    }
}
