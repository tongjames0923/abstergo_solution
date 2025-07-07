package com.tbs.boardcast.enums;

import com.tbs.common.BaseEnum;

/**
 * 事件状态枚举
 *
 * @author abstergo
 */

public enum EventStatusEnum implements BaseEnum<String> {
    IDLE("空闲", 0), IN_QUEUE("等待处理", 1), IN_PROCESS("处理中", 2), DONE("处理完成", 3), FAILED("处理失败", 4),
    CANCELED("取消处理", 5), REQUIRE_CANCEL("需要取消处理", 6), REQUIRE_RETRY("需要重试", 7), RETRYING("正在重试", 8),
    ;

    EventStatusEnum(String value, int code) {
        this.value = value;
        this.code = code;
    }

    private String value;
    private int code;

    @Override
    public String getValue() {
        return "";
    }

    @Override
    public int getCode() {
        return 0;
    }
}
