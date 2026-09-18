package com.tbs.push.model;

import com.tbs.common.BaseEnum;

/**
 * 投递结果枚举
 * <p>
 * 描述一次投递动作的终态，投递记录按此留痕：投递一次即可，失败不做自动重试。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public enum DeliveryResultEnum implements BaseEnum<String> {

    /**
     * 投递成功：帧已写入传输通道
     */
    DELIVERED("delivered"),

    /**
     * 投递失败：传输通道写入异常（连接已断开等），失败仅如实记录不重试
     */
    FAILED("failed"),

    /**
     * 无投递目标：协调器未解析到任何在线目标会话
     */
    NO_SUBSCRIBER("no_subscriber");

    /**
     * 结果小写值
     */
    private final String value;

    /**
     * 构造投递结果枚举
     *
     * @param value 结果小写值
     */
    DeliveryResultEnum(String value) {
        this.value = value;
    }

    /**
     * 获取结果小写值
     *
     * @return 结果小写值
     */
    @Override
    public String getValue() {
        return value;
    }

    /**
     * 获取枚举编码（按声明顺序，从1开始）
     *
     * @return 枚举编码
     */
    @Override
    public Integer getCode() {
        return ordinal() + 1;
    }

}
