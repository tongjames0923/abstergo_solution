package com.tbs.mq.v2.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 消息主记录VO（v2 记录驱动模型）
 * <p>
 * 消费链路的核心载体：接收段建档的主记录、内存待处理队列元素、单飞抢占对象均为该VO。
 * 双层设计：{@code rawMessage}（R）为厂商原始消息，仅留档供排查与重放；{@code payload}（T）为
 * 子类转换后的业务负载，消费链路只传递负载，消费者不感知厂商类型。
 * </p>
 *
 * @param <R> 厂商原始消息类型（传输层）
 * @param <T> 业务负载类型（消费者面对的类型）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MqMessageVO<R, T extends Serializable> implements Serializable {

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 业务id
     */
    private String businessId;

    /**
     * 原始消息（厂商传输层消息，仅留档，不下发给消费者）
     */
    private R rawMessage;

    /**
     * 业务负载（由子类 {@code getPayload} 转换，消费者面对的对象）
     */
    private T payload;


    /**
     * 消息标志
     *
     * @see com.tbs.mq.v2.enums.MqMessageStatusEnum
     */
    private Integer flag;

}
