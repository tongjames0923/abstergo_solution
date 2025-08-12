package com.tbs.broadcast.idempotency;

import com.tbs.common.BaseEnum;

/**
 * 事件幂等性处理结果
 *
 * @author abstergo
 */
public enum EventIdempotencyResult implements BaseEnum<String> {
    /**
     * 需要处理 - 事件未被处理过
     */
    NEEDS_PROCESSING(2, "需要处理"),

    /**
     * 已处理成功 - 事件已被成功处理
     */
    ALREADY_PROCESSED_SUCCESS(1, "已处理成功"),

    /**
     * 处理中 - 事件正在被处理
     */
    BEING_PROCESSED(0, "处理中"),

    /**
     * 处理失败 - 事件之前处理失败
     */
    PREVIOUSLY_FAILED(-1, "处理失败");

    private Integer code;
    private String value;
    EventIdempotencyResult(Integer code, String value) {
        this.code = code;
        this.value = value;
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
