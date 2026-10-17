package com.tbs.mq.v2.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 消费行VO（v2 记录驱动模型）
 * <p>
 * 一条消息对每一个消费者展开一行，行是处理、单飞、状态推进与重试回灌的最小工作单元： 行身份 = messageId + consumeId，单行失败不影响同消息其他行，重试只回灌失败行对应的消费者。
 * </p>
 *
 * @param <T> 厂商原始消息类型
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
     * 消费者ID，对应 {@code IMqConsumer.consumeId()}
     */
    private Integer consumeId;

    /**
     * 业务标识，继承自主记录
     */
    private String businessId;

    /**
     * 厂商原始消息
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
     * 行状态
     *
     * @see com.tbs.mq.v2.enums.MqMessageConsumeStatusEnum
     */
    private Integer flag;

    /**
     * 异常
     */
    private Exception exception;

}
