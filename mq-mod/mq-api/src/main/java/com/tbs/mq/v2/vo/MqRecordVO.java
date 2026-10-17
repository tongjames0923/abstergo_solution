package com.tbs.mq.v2.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 消息建档结果VO
 * <p>
 * {@code IMqRecorder.record} 批量建档的逐条结果，通过 messageId 与入参消息VO配对，
 * 支持部分成功语义：单条建档失败不影响同批其他消息。
 * </p>
 *
 * @param <T> 业务负载类型
 */
@Data
public class MqRecordVO<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID，与入参消息VO的 messageId 一致，配对与状态推进的基准键
     */
    private String messageId;

    /**
     * 消息体（业务负载）
     */
    private T body;

    /**
     * 建档是否成功
     */
    private boolean success;

    /**
     * 建档失败时的原因异常，成功时为空
     */
    private Exception exception;
}
