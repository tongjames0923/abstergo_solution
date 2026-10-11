package com.tbs.cdc.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tbs.cdc.service.property.CdcMqProperty;
import com.tbs.cdc.service.publisher.CdcMqPublisher;
import com.tbs.mq.api.producer.IMqProducer;
import com.tbs.rabbitmq.producer.RabbitMqProducer;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CDC服务装配配置
 * <p>
 * {@code tbs.cdc.mq.enable}开启时装配MQ发送实现与CDC MQ发出监听器；
 * 关闭后服务仅保留CDC捕获与进程内分发能力。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "tbs.cdc.mq", name = "enable", matchIfMissing = true)
@EnableConfigurationProperties(CdcMqProperty.class)
public class CdcServiceConfig {

    /**
     * 创建MQ消息发送实现
     * <p>
     * 复用RabbitMQ装配提供的RabbitTemplate与消息转换器，落地统一发送契约。
     * </p>
     *
     * @param rabbitTemplate   RabbitMQ发送模板
     * @param messageConverter 消息转换器
     * @return MQ消息发送实现
     */
    @Bean
    public IMqProducer cdcMqProducer(RabbitTemplate rabbitTemplate, MessageConverter messageConverter) {
        return new RabbitMqProducer(rabbitTemplate, messageConverter);
    }

    /**
     * 创建CDC MQ发出监听器
     *
     * @param cdcMqProducer     MQ消息发送契约
     * @param cdcMqProperty     MQ发出配置
     * @param objectMapper      JSON序列化器
     * @param amqpAdminProvider AMQP管理器提供者
     * @return CDC MQ发出监听器
     */
    @Bean
    public CdcMqPublisher cdcMqPublisher(IMqProducer cdcMqProducer, CdcMqProperty cdcMqProperty,
                                         ObjectMapper objectMapper, ObjectProvider<AmqpAdmin> amqpAdminProvider) {
        return new CdcMqPublisher(cdcMqProducer, cdcMqProperty, objectMapper, amqpAdminProvider);
    }
}
