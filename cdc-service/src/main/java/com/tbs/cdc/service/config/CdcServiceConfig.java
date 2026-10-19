package com.tbs.cdc.service.config;

import com.tbs.cdc.service.property.CdcMqProperty;
import com.tbs.cdc.service.publisher.CdcMqPublisher;
import com.tbs.mq.v2.producer.IMqMessageProducer;
import org.springframework.amqp.core.AmqpAdmin;
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
     * 创建CDC MQ发出监听器
     *
     * @param mqMessageProducer v2统一消息发送契约（由 {@code @EnableRabbitMq} 装配提供）
     * @param cdcMqProperty     MQ发出配置
     * @param amqpAdminProvider AMQP管理器提供者
     * @return CDC MQ发出监听器
     */
    @Bean
    public CdcMqPublisher cdcMqPublisher(IMqMessageProducer mqMessageProducer, CdcMqProperty cdcMqProperty,
        ObjectProvider<AmqpAdmin> amqpAdminProvider) {
        return new CdcMqPublisher(mqMessageProducer, cdcMqProperty, amqpAdminProvider);
    }
}
