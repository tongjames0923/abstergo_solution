package com.tbs.mq.v2.enums;

import com.tbs.common.BaseEnum;

/**
 * 消费台账状态枚举（v2 记录驱动模型 · 消费可靠性依据）
 * <p>
 * 描述单个消费者（messageId + consumeId 定位一行）对本次消息分发的事后结果；
 * 重试扫描据 FAIL 行判断回灌目标，只重跑失败消费者，不重跑已成功消费者。
 * 与 {@link MqMessageStatusEnum} 的主记录状态机分属两层，互不覆盖。
 * </p>
 */
public enum MqMessageConsumeStatusEnum implements BaseEnum<String> {
    /**
     * 消费成功
     */
    SUCCESS("成功", 1),
    /**
     * 消费失败，携带异常原因供回灌重试判定
     */
    FAIL("失败", 0);
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
