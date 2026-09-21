package com.tbs.push.model.message;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 投递记录
 * <p>
 * 分发器对每一条消息的每一次投递动作（含无目标场景）产生的完整留痕， 经{@code IDeliveryRecordStore}持久化，保证投递行为可审计、可追溯。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class DeliveryRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录唯一标识，建议使用UUID
     */
    private String id;

    /**
     * 关联的消息唯一标识（即消息与DATA帧的messageId）
     */
    private String messageId;

    /**
     * 投递主题
     */
    private String topic;

    /**
     * 目标会话标识，无投递目标时为空
     */
    private String sessionId;

    /**
     * 目标会话的传输类型
     */
    private String transportType;

    /**
     * 投递结果
     */
    private DeliveryResultEnum result;

    /**
     * 投递时间
     */
    private Date deliverTime;

    /**
     * 失败原因摘要，成功时为空
     */
    private String errorMessage;

}
