package com.tbs.rabbitmq.producer;

import com.tbs.common.util.base.StrUtil;
import com.tbs.mq.api.producer.AbstractMqProducer;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;

/**
 * RabbitMQ消息发送实现
 * <p>
 * 基于{@link RabbitTemplate}落地统一发送契约：
 * <ul>
 *     <li>业务对象payload经{@link MessageConverter}转换后发送</li>
 *     <li>payload本身已是原始{@link Message}时直接发送，仅在messageId缺失时补齐</li>
 *     <li>messageId统一写入消息属性，供消费端解析幂等键</li>
 * </ul>
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public class RabbitMqProducer extends AbstractMqProducer {

    /**
     * RabbitMQ发送模板
     */
    private final RabbitTemplate rabbitTemplate;

    /**
     * 消息转换器
     */
    private final MessageConverter messageConverter;

    /**
     * 构造RabbitMQ消息发送实现
     *
     * @param rabbitTemplate   RabbitMQ发送模板
     * @param messageConverter 消息转换器
     */
    public RabbitMqProducer(RabbitTemplate rabbitTemplate, MessageConverter messageConverter) {
        this.rabbitTemplate = rabbitTemplate;
        this.messageConverter = messageConverter;
    }

    /**
     * 实际发送动作
     * <p>
     * 保证messageId写入消息属性；路由键为空时按空串处理（适配扇出交换机）。
     * </p>
     *
     * @param exchangeName 交换机名称
     * @param routingKey   路由键
     * @param payload      消息体，支持业务对象或已组装的原始{@link Message}
     * @param messageId    最终确定的消息唯一标识，保证非空
     */
    @Override
    protected void doSend(String exchangeName, String routingKey, Object payload, String messageId) {
        Message message;
        if (payload instanceof Message) {
            message = (Message) payload;
            if (StrUtil.isEmpty(message.getMessageProperties().getMessageId())) {
                message.getMessageProperties().setMessageId(messageId);
            }
        } else {
            MessageProperties properties = new MessageProperties();
            properties.setMessageId(messageId);
            message = messageConverter.toMessage(payload, properties);
        }
        rabbitTemplate.send(exchangeName, routingKey == null ? "" : routingKey, message);
    }

}
