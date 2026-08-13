package com.tbs.mq.api.producer;

import com.tbs.common.util.base.StrUtil;

import java.util.UUID;

/**
 * MQ消息发送基类
 * <p>
 * 封装messageId的统一处理规则：
 * <ul>
 *     <li>调用方未提供messageId时自动生成</li>
 *     <li>显式提供的messageId原样保留</li>
 * </ul>
 * 具体MQ厂商的实现只需覆盖{@link #doSend(String, String, Object, String)}，
 * 将payload与messageId落地到具体的发送通道，无需关心messageId生成逻辑。
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public abstract class AbstractMqProducer implements IMqProducer {

    /**
     * 发送消息（自动生成messageId）
     *
     * @param exchangeName 交换机名称
     * @param routingKey   路由键
     * @param payload      消息体
     */
    @Override
    public final void send(String exchangeName, String routingKey, Object payload) {
        send(exchangeName, routingKey, payload, null);
    }

    /**
     * 发送消息（显式messageId，为空时自动生成）
     *
     * @param exchangeName 交换机名称
     * @param routingKey   路由键
     * @param payload      消息体
     * @param messageId    消息唯一标识，空则自动生成
     */
    @Override
    public final void send(String exchangeName, String routingKey, Object payload, String messageId) {
        String finalMessageId = StrUtil.isEmpty(messageId) ? generateMessageId() : messageId;
        doSend(exchangeName, routingKey, payload, finalMessageId);
    }

    /**
     * 具体发送实现
     * <p>
     * 实现方必须保证messageId被写入消息属性（如AMQP的MessageProperties.messageId）， 以供消费端解析幂等键。
     * </p>
     *
     * @param exchangeName 交换机名称
     * @param routingKey   路由键
     * @param payload      消息体
     * @param messageId    最终确定的消息唯一标识，保证非空
     */
    protected abstract void doSend(String exchangeName, String routingKey, Object payload, String messageId);

    /**
     * 生成messageId
     * <p>
     * 默认使用UUID，子类可覆盖以接入雪花算法等分布式ID生成器。
     * </p>
     *
     * @return 消息唯一标识
     */
    protected String generateMessageId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
