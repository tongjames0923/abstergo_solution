package com.tbs.rabbitmq.config;

import com.tbs.mq.api.consumer.IMessageRepublisher;
import com.tbs.mq.api.producer.IMqProducer;
import com.tbs.mq.api.retry.IFailedMessageStore;
import com.tbs.mq.api.retry.IRetryDelayPolicy;
import com.tbs.rabbitmq.producer.RabbitMqProducer;
import com.tbs.rabbitmq.retry.RabbitMqMessageRepublisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ厂商实现组件装配
 * <p>
 * 仅提供依赖RabbitMQ厂商SDK的默认实现，全部允许被使用方覆盖：
 * <ul>
 *     <li>{@link IMqProducer} — 基于RabbitTemplate的统一发送实现</li>
 *     <li>{@link IMessageRepublisher} — 失败消息还原与重投递实现</li>
 * </ul>
 * 厂商无关的默认装配（重试延迟策略、失败消息存储兜底、重投递调度器）
 * 已上移至mq-api的{@code com.tbs.mq.config.MqSupportConfig}。
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
@Configuration
public class RabbitMqSupportConfig {

    /**
     * 统一消息发送实现
     *
     * @param rabbitTemplate   RabbitMQ发送模板
     * @param messageConverter 消息转换器
     * @return 默认消息发送实现
     */
    @Bean
    @ConditionalOnMissingBean(IMqProducer.class)
    public IMqProducer mqProducer(RabbitTemplate rabbitTemplate, MessageConverter messageConverter) {
        return new RabbitMqProducer(rabbitTemplate, messageConverter);
    }

    /**
     * 失败消息重投递实现
     *
     * @param failedMessageStore 失败消息存储
     * @param retryDelayPolicy   重试延迟策略
     * @param rabbitTemplate     RabbitMQ发送模板
     * @return 默认重投递实现
     */
    @Bean
    @ConditionalOnMissingBean(IMessageRepublisher.class)
    public IMessageRepublisher messageRepublisher(IFailedMessageStore failedMessageStore,
                                                  IRetryDelayPolicy retryDelayPolicy,
                                                  RabbitTemplate rabbitTemplate) {
        return new RabbitMqMessageRepublisher(failedMessageStore, retryDelayPolicy, rabbitTemplate);
    }

}
