package com.tbs.mq.v2.enums;

import com.tbs.common.BaseEnum;

/**
 * 消息主记录状态枚举（v2 记录驱动模型 · 投递可靠性真源）
 * <p>
 * 描述一条消息在体系内的生命周期：接收建档 → 处理线程抢占分发 → 终态。
 * 状态机：RECEIVED → PROCESSING → PROCESSED/FAILED；其中 PROCESSED 仅代表分发完成，
 * 不等于全部消费者消费成功（逐消费者成败见 {@code MqConsumeVO} 台账），FAILED 为分发级失败留痕、待回灌。
 * </p>
 */
public enum MqMessageStatusEnum implements BaseEnum<String> {
    /**
     * 已接收：建档成功进入待处理队列的初始态
     */
    RECEIVED(0, "received"),
    /**
     * 处理中：已被处理线程抢占并正在分发
     */
    PROCESSING(1, "processing"),
    /**
     * 已处理：分发完成（不代表全员消费成功）
     */
    PROCESSED(2, "processed"),
    /**
     * 已失败：分发级异常留痕，等待记录层回灌重放
     */
    FAILED(3, "failed");

    /**
     * 存储编码，落库使用
     */
    private Integer code;
    /**
     * 状态描述值
     */
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
