package com.tbs.rabbitmq.config;

import com.tbs.rabbitmq.property.RabbitMqProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ初始化器
 * <p>
 * 提供动态创建交换机、队列和绑定关系的能力
 * 交换机、队列和绑定关系的初始声明由TbsRabbitMqConfig中的Bean定义自动完成
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
@Component
@Slf4j
public class RabbitMqInitializer {

    private final AmqpAdmin amqpAdmin;
    private final RabbitMqProperty rabbitMqProperty;

    /**
     * 构造方法
     *
     * @param amqpAdmin       AMQP管理器（由Spring管理）
     * @param rabbitMqProperty RabbitMQ配置属性
     */
    public RabbitMqInitializer(AmqpAdmin amqpAdmin, RabbitMqProperty rabbitMqProperty) {
        this.amqpAdmin = amqpAdmin;
        this.rabbitMqProperty = rabbitMqProperty;
    }

    /**
     * 动态创建交换机
     * <p>
     * 创建持久化、非自动删除的直接交换机
     * </p>
     *
     * @param exchangeName 交换机名称
     */
    public void createExchange(String exchangeName) {
        DirectExchange exchange = new DirectExchange(exchangeName, true, false);
        amqpAdmin.declareExchange(exchange);
    }

    /**
     * 动态创建队列
     * <p>
     * 创建持久化队列，使用配置中的TTL和高可用策略
     * </p>
     *
     * @param queueName 队列名称
     */
    public void createQueue(String queueName) {
        Queue queue = QueueBuilder.durable(queueName)
            .withArgument("x-message-ttl", rabbitMqProperty.getMessageTtl())
            .withArgument("x-ha-policy", "all")
            .build();
        amqpAdmin.declareQueue(queue);
    }

    /**
     * 动态创建绑定关系
     * <p>
     * 在指定的交换机和队列之间创建绑定关系
     * </p>
     *
     * @param exchangeName 交换机名称
     * @param queueName    队列名称
     * @param routingKey   路由键
     */
    public void createBinding(String exchangeName, String queueName, String routingKey) {
        Binding binding =
            BindingBuilder.bind(new Queue(queueName)).to(new DirectExchange(exchangeName)).with(routingKey);
        amqpAdmin.declareBinding(binding);
    }
}
