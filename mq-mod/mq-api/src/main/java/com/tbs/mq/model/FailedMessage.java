package com.tbs.mq.model;

import com.tbs.mq.api.retry.IFailedMessageStore;
import com.tbs.mq.api.retry.IRetryDelayPolicy;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 失败消息记录
 * <p>
 * 消费失败后被持久化到{@link IFailedMessageStore}的完整消息快照，
 * 包含重投递所需的全部信息：目标路由、原始消息体、消息头与重试状态。
 * 消息体以原始字节形式保存，重投递时还原为原始消息，避免二次序列化带来的转换兼容问题。
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
@Data
public class FailedMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录唯一标识，建议使用UUID
     */
    private String id;

    /**
     * 原消费队列名称
     */
    private String queueName;

    /**
     * 重投递目标交换机名称
     */
    private String exchangeName;

    /**
     * 重投递目标路由键
     */
    private String routingKey;

    /**
     * 原始消息体字节
     */
    private byte[] body;

    /**
     * 消息内容类型（对应原消息的contentType），可为空
     */
    private String contentType;

    /**
     * 原始消息头快照，重投递时原样恢复（重试次数头由调度器更新）
     */
    private Map<String, Object> headers;

    /**
     * 原消息的messageId，重投递时保留以维持消费端幂等键连续
     */
    private String messageId;

    /**
     * 已重试次数（首次入库时为0）
     */
    private int retryCount;

    /**
     * 首次失败时间
     */
    private Date firstFailTime;

    /**
     * 最近一次失败的原因摘要
     */
    private String errorMessage;

    /**
     * 下次允许重投递的时间，由{@link IRetryDelayPolicy}计算得出
     */
    private Date nextRetryTime;

}
