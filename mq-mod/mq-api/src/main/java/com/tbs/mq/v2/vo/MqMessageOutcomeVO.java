package com.tbs.mq.v2.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 消息发送VO
 *
 * @param <T> 消息体类型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MqMessageOutcomeVO<T extends Serializable> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 消息id
     */
    private String messageId;

    /**
     * 业务id
     */
    private String businessId;

    /**
     * 消息体格式
     */
    private String payloadFormat;

    /**
     * 业务负载的全限定类名，供消费端将字节反序列化回目标类型；
     * 为空时由生产端取 {@code payload.getClass().getName()} 兜底
     */
    private String payloadType;

    /**
     * 主题
     */
    private String topic;

    /**
     * 路由key
     */
    private String routeKey;

    /**
     * 消息体
     */
    private T payload;

    /**
     * 消息头
     */
    private Map<String, String> headers;

}
