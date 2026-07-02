package com.tbs.rabbitmq.config;

import com.tbs.rabbitmq.property.RabbitMqProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SerializerMessageConverter;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

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
    private RabbitMqProperty rabbitMqProperty;

    @Resource
    private RabbitProperties rabbitProperties;

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
     *
     * @return SerializerMessageConverter实例
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new SerializerMessageConverter();
    }

    /**
     * 创建广播交换机
     *
     * @return DirectExchange实例
     */
    @Bean
    public DirectExchange broadcastExchange() {
        return new DirectExchange(rabbitMqProperty.getExchangeName(), true, false);
    }

    /**
     * 创建死信交换机（仅当配置了deadLetterExchange时生效）
     *
     * @return DirectExchange实例
     */
    @Bean
    @ConditionalOnProperty(prefix = "tbs.rabbitmq", name = "dead-letter-exchange")
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(rabbitMqProperty.getDeadLetterExchange(), true, false);
    }

    /**
     * 创建死信队列（仅当配置了deadLetterQueue时生效）
     *
     * @return Queue实例
     */
    @Bean
    @ConditionalOnProperty(prefix = "tbs.rabbitmq", name = "dead-letter-queue")
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(rabbitMqProperty.getDeadLetterQueue()).build();
    }

    /**
     * 绑定死信队列到死信交换机（仅当死信交换机和死信队列均已配置时生效）
     *
     * @param deadLetterExchange 死信交换机
     * @param deadLetterQueue    死信队列
     * @return Binding实例
     */
    @Bean
    @ConditionalOnProperty(prefix = "tbs.rabbitmq", name = {"dead-letter-exchange", "dead-letter-queue"})
    public Binding deadLetterBinding(DirectExchange deadLetterExchange, Queue deadLetterQueue) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(rabbitMqProperty.getRoutingKey());
    }

    /**
     * 创建广播队列
     * <p>
     * 配置消息TTL、高可用策略，以及可选的死信交换机
     * </p>
     *
     * @return Queue实例
     */
    @Bean
    public Queue broadcastQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", rabbitMqProperty.getMessageTtl());
        args.put("x-ha-policy", "all");

        String dlxName = rabbitMqProperty.getDeadLetterExchange();
        if (dlxName != null && !dlxName.isBlank()) {
            args.put("x-dead-letter-exchange", dlxName);
            args.put("x-dead-letter-routing-key", rabbitMqProperty.getRoutingKey());
        }

        return QueueBuilder.durable(rabbitMqProperty.getQueueName())
            .withArguments(args)
            .build();
    }

    /**
     * 绑定队列到交换机
     *
     * @param broadcastQueue    广播队列
     * @param broadcastExchange 广播交换机
     * @return Binding实例
     */
    @Bean
    public Binding broadcastBinding(Queue broadcastQueue, DirectExchange broadcastExchange) {
        return BindingBuilder.bind(broadcastQueue).to(broadcastExchange).with(rabbitMqProperty.getRoutingKey());
    }

}
