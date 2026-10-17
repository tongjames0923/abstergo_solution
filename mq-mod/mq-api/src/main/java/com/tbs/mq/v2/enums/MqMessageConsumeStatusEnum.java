package com.tbs.mq.v2.enums;

import com.tbs.common.BaseEnum;

public enum MqMessageConsumeStatusEnum implements BaseEnum<String> {
    SUCCESS("成功", 1), FAIL("失败", 0);
    private String value;
    private Integer code;

    MqMessageConsumeStatusEnum(String value, Integer code) {
        this.value = value;
        this.code = code;
    }

    /**
     * 获取枚举详情值
     * <p>
     * 返回枚举项的详细描述信息，通常是一个具有业务含义的字符串， 用于在界面展示或日志记录中提供更友好的信息。
     *
     * @return 枚举详情值
     */
    @Override
    public String getValue() {
        return value;
    }

    /**
     * 获取枚举编码
     * <p>
     * 返回枚举项的整数编码，通常用于数据库存储或系统内部处理， 编码值应该是唯一的，便于程序识别和处理不同的枚举项。
     *
     * @return 枚举编码值
     */
    @Override
    public Integer getCode() {
        return code;
    }
}
