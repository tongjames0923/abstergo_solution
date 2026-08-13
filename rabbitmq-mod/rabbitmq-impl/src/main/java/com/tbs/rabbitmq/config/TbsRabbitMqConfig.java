package com.tbs.rabbitmq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SerializerMessageConverter;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * TBS RabbitMQ配置类
 * <p>
 * 提供RabbitMQ相关组件的配置，包括连接工厂、RabbitTemplate、交换机、队列和绑定关系
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class TbsRabbitMqConfig {

    @Resource
    private RabbitProperties rabbitProperties;

    /**
     * 允许反序列化的类名前缀列表
     */
    private static final List<String> TRUSTED_PACKAGES = Arrays.asList("*");

    /**
     * 创建RabbitMQ连接工厂
     * <p>
     * 作为Spring Boot自动配置的备选方案，当容器中不存在ConnectionFactory时生效
     * </p>
     *
     * @return ConnectionFactory实例
     */
    @Bean
    @ConditionalOnMissingBean(ConnectionFactory.class)
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();

        if (rabbitProperties.getHost() != null) {
            connectionFactory.setHost(rabbitProperties.getHost());
        } else {
            connectionFactory.setHost("localhost");
        }

        connectionFactory.setPort(rabbitProperties.getPort());

        if (rabbitProperties.getUsername() != null) {
            connectionFactory.setUsername(rabbitProperties.getUsername());
        }

        if (rabbitProperties.getPassword() != null) {
            connectionFactory.setPassword(rabbitProperties.getPassword());
        }

        if (rabbitProperties.getVirtualHost() != null) {
            connectionFactory.setVirtualHost(rabbitProperties.getVirtualHost());
        } else {
            connectionFactory.setVirtualHost("/");
        }

        connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        connectionFactory.setCloseTimeout(10000);
        connectionFactory.setConnectionTimeout(10000);
        return connectionFactory;
    }

    /**
     * 创建RabbitTemplate用于发送消息
     *
     * @param connectionFactory 连接工厂
     * @param messageConverter  消息转换器
     * @return RabbitTemplate实例
     */
    @Bean
    @ConditionalOnMissingBean(RabbitTemplate.class)
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.error("RabbitMQ消息发送失败: {}", cause);
            }
        });
        return template;
    }

    /**
     * 创建Rabbit管理器
     *
     * @param connectionFactory 连接工厂
     * @return AmqpAdmin实例
     */
    @Bean
    @ConditionalOnMissingBean(AmqpAdmin.class)
    public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    /**
     * 创建序列化消息转换器
     * <p>
     * 配置允许反序列化的类名模式，解决Spring AMQP 2.4+的反序列化安全限制。
     * 容器中已存在MessageConverter时不重复注册。
     * </p>
     *
     * @return SerializerMessageConverter实例
     */
    @Bean
    @ConditionalOnMissingBean(MessageConverter.class)
    public MessageConverter jsonMessageConverter() {
        SerializerMessageConverter converter = new SerializerMessageConverter();
        converter.setAllowedListPatterns(TRUSTED_PACKAGES);
        log.info("RabbitMQ消息转换器已配置，允许反序列化的包: {}", TRUSTED_PACKAGES);
        return converter;
    }

}
