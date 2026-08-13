package com.tbs.mq.api.producer;

/**
 * MQ消息发送契约
 * <p>
 * 框架内唯一推荐的消息发送出口。所有实现必须保证：
 * <ul>
 *     <li>发送前若消息未携带messageId，则自动补齐（消费端幂等键的兜底来源）</li>
 *     <li>调用方显式提供messageId时保留原值，不做覆盖</li>
 * </ul>
 * 本接口不依赖任何MQ厂商类型，payload可以是业务对象，也可以是已组装好的原始消息对象。
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public interface IMqProducer {

    /**
     * 发送消息到指定的交换机和路由键
     * <p>
     * 未显式指定messageId时由实现自动生成。
     * </p>
     *
     * @param exchangeName 交换机名称
     * @param routingKey   路由键
     * @param payload      消息体
     */
    void send(String exchangeName, String routingKey, Object payload);

    /**
     * 发送消息到指定的交换机和路由键，并显式指定messageId
     * <p>
     * messageId为空时退化为自动生成。重投递场景应传入原消息的messageId，
     * 以保证消费端幂等键的连续性。
     * </p>
     *
     * @param exchangeName 交换机名称
     * @param routingKey   路由键
     * @param payload      消息体
     * @param messageId    消息唯一标识
     */
    void send(String exchangeName, String routingKey, Object payload, String messageId);

}
