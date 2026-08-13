package com.tbs.rabbitmq.retry;

import com.tbs.mq.api.consumer.AbstractMessageRepublisher;
import com.tbs.mq.api.retry.IFailedMessageStore;
import com.tbs.mq.api.retry.IRetryDelayPolicy;
import com.tbs.mq.model.FailedMessage;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * RabbitMQ失败消息重投递实现
 * <p>
 * 将{@link FailedMessage}快照还原为原始消息（消息体字节、消息头、contentType与messageId原样恢复），
 * 经{@link RabbitTemplate}重新发布到原交换机与路由键。
 * 重试次数头的递增与存储记账由基类模板统一完成。
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public class RabbitMqMessageRepublisher extends AbstractMessageRepublisher {

    /**
     * RabbitMQ发送模板
     */
    private final RabbitTemplate rabbitTemplate;

    /**
     * 构造RabbitMQ重投递实现
     *
     * @param failedMessageStore 失败消息存储
     * @param retryDelayPolicy   重试延迟策略
     * @param rabbitTemplate     RabbitMQ发送模板
     */
    public RabbitMqMessageRepublisher(IFailedMessageStore failedMessageStore, IRetryDelayPolicy retryDelayPolicy,
                                      RabbitTemplate rabbitTemplate) {
        super(failedMessageStore, retryDelayPolicy);
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 实际重投递动作：还原原始消息并发布
     *
     * @param failedMessage 已递增重试状态的消息记录
     * @return true表示发布成功
     */
    @Override
    protected boolean doRepublish(FailedMessage failedMessage) {
        try {
            MessageProperties properties = new MessageProperties();
            if (failedMessage.getHeaders() != null) {
                properties.getHeaders().putAll(failedMessage.getHeaders());
            }
            properties.setMessageId(failedMessage.getMessageId());
            if (failedMessage.getContentType() != null) {
                properties.setContentType(failedMessage.getContentType());
            }

            byte[] body = failedMessage.getBody() == null ? new byte[0] : failedMessage.getBody();
            Message message = new Message(body, properties);

            String routingKey = failedMessage.getRoutingKey() == null ? "" : failedMessage.getRoutingKey();
            rabbitTemplate.send(failedMessage.getExchangeName(), routingKey, message);
            return true;
        } catch (Exception e) {
            log.error("消息重投递发布失败, id:{}, exchange:{}, routingKey:{}", failedMessage.getId(),
                failedMessage.getExchangeName(), failedMessage.getRoutingKey(), e);
            return false;
        }
    }

}
