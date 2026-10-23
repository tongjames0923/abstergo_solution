package com.tbs.mq.v2.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 消费台账VO（v2 记录驱动模型）
 * <p>
 * 记录“消息 × 消费者”单次分发的事后结果（messageId + consumeId 为身份键，起止时间 + 成败状态 + 异常）：
 * 主记录 PROCESSED 仅代表分发完成，各消费者是否真正消费成功以台账为准；
 * 重试扫描基于台账的 FAIL 行判断回灌目标，只重跑失败消费者，不重跑已成功消费者。
 * </p>
 *
 * @param <T> 业务负载类型
 * @author tongj
 * @since 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MqConsumeVO<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID，来自建档成功的主记录
     */
    private String messageId;

    /**
     * 消费者ID，对应 {@code IMqConsumer.consumeId()}，台账行身份键组成部分
     */
    private Integer consumeId;

    /**
     * 业务标识，继承自主记录
     */
    private String businessId;

    /**
     * 业务负载（当前未赋值，回灌时由 messageId 经主记录回查取得）
     */
    private T payload;

    /**
     * 消费时间
     */
    private Date consumeStartTime;

    /**
     * 消费完成时间
     */
    private Date consumeEndTime;

    /**
     * 行状态（成/败）
     *
     * @see com.tbs.mq.v2.enums.MqMessageConsumeStatusEnum
     */
    private Integer flag;

    /**
     * 消费失败时的原因异常，成功时为空；落库时由实现方序列化为字符串留痕
     */
    private Exception exception;

}
