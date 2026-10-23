package com.tbs.rabbitmq.v2;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.framework.UuidUtil;
import com.tbs.mq.constants.MqHeaderConstants;
import com.tbs.mq.v2.core.IMqMessagePayloadConverter;
import com.tbs.mq.v2.core.MqPayloadConverterRegistry;
import com.tbs.mq.v2.producer.IMqMessageProducer;
import com.tbs.mq.v2.vo.MqMessageOutcomeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Map;

/**
 * RabbitMQ 消息发送实现（v2 记录驱动模型 · 生产段的 RabbitMQ 落地）
 * <p>
 * 把 v2 厂商无关信封 {@link MqMessageOutcomeVO} 落地为 Spring AMQP {@link Message} 并经 {@link RabbitTemplate} 投递，与
 * {@link RabbitMqMessageReceiver} 的接收段严格对称：
 * <ul>
 *     <li>messageId：信封为空时以框架 UUID 兜底发号，写入厂商消息属性，供消费端 {@code genMessageId} 复用去重；</li>
 *     <li>payload：按 {@code payloadFormat}（空则默认格式）选定的 {@link IMqMessagePayloadConverter} 序列化为字节；</li>
 *     <li>消息头：写入 {@code payloadType}（信封为空取 payload 实际类名）、{@code payloadFormat}、
 *         {@code businessId}（非空时）及信封自定义 headers，供消费端反序列化与业务标识解析。</li>
 * </ul>
 * 组装与发送分离：{@link #toVendorMessage(MqMessageOutcomeVO)} 只做信封→消息转换（可离线自检），
 * {@link #send(MqMessageOutcomeVO)} 在其上补投递。非注解类，由 config 以 {@code @Bean} 装配。
 * </p>
 *
 * @author tongj
 * @since 2.0.0
 */
@Slf4j
public class RabbitMqMessageProducer implements IMqMessageProducer {

    /**
     * RabbitMQ 发送模板
     */
    private final RabbitTemplate rabbitTemplate;

    /**
     * 负载转换器注册表，按 payloadFormat 选取序列化实现
     */
    private final MqPayloadConverterRegistry converterRegistry;

    /**
     * 构造 RabbitMQ v2 消息发送实现
     *
     * @param rabbitTemplate    RabbitMQ 发送模板，不为空
     * @param converterRegistry 负载转换器注册表，不为空
     */
    public RabbitMqMessageProducer(RabbitTemplate rabbitTemplate, MqPayloadConverterRegistry converterRegistry) {
        this.rabbitTemplate = rabbitTemplate;
        this.converterRegistry = converterRegistry;
    }

    /**
     * 发送消息：组装为厂商消息后按 topic（交换机）/routeKey 投递
     *
     * @param message 待发送的消息信封，不为空
     */
    @Override
    public void send(MqMessageOutcomeVO message) {
        AssertUtil.isTrueSystemError(message == null).i18n("mq.v2.outcome.message.required").throwIt();
        Message vendorMessage = toVendorMessage(message);
        String exchange = StrUtil.isEmpty(message.getTopic()) ? "" : message.getTopic();
        String routingKey = StrUtil.isEmpty(message.getRouteKey()) ? "" : message.getRouteKey();
        log.debug("[MQ-v2] send message, exchange={}, routingKey={}, messageId={}, payloadType={}", exchange,
            routingKey, vendorMessage.getMessageProperties().getMessageId(), message.getPayloadType());
        rabbitTemplate.send(exchange, routingKey, vendorMessage);
    }

    /**
     * 将信封组装为厂商消息（不投递，供离线自检）
     * <p>
     * messageId 空则框架发号；payload 按格式序列化；payloadType/payloadFormat/businessId 与自定义 headers 落消息头。
     * </p>
     *
     * @param message 消息信封，payload 不为空
     * @return 组装好的厂商消息
     */
    @SuppressWarnings("unchecked")
    public Message toVendorMessage(MqMessageOutcomeVO message) {
        Object payload = message.getPayload();
        String payloadType =
            StrUtil.isEmpty(message.getPayloadType()) ? payload.getClass().getName() : message.getPayloadType();
        String messageId =
            StrUtil.isEmpty(message.getMessageId()) ? UuidUtil.getInstance().getUuidString() : message.getMessageId();

        IMqMessagePayloadConverter converter = converterRegistry.resolve(message.getPayloadFormat());
        byte[] body = converter.convert(payload, (Class<Object>)payload.getClass());
        MessageProperties properties = new MessageProperties();
        properties.setMessageId(messageId);
        String contentType = converter.contentType();
        if (StrUtil.isNotEmpty(contentType)) {
            properties.setContentType(contentType);
        }
        properties.setHeader(MqHeaderConstants.PAYLOAD_TYPE, payloadType);
        properties.setHeader(MqHeaderConstants.PAYLOAD_FORMAT, converter.payloadFormat());
        if (StrUtil.isNotEmpty(message.getBusinessId())) {
            properties.setHeader(MqHeaderConstants.BUSINESS_ID, message.getBusinessId());
        }
        Map<String, String> headers = message.getHeaders();
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                properties.setHeader(entry.getKey(), entry.getValue());
            }
        }
        return new Message(body, properties);
    }
}
