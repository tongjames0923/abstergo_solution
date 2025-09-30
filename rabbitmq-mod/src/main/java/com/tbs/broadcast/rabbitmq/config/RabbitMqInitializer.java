package com.tbs.broadcast.rabbitmq.config;

import com.tbs.broadcast.rabbitmq.config.property.RabbitMqProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * RabbitMQ初始化器 用于在应用程序启动时通过代码直接创建交换机、队列和绑定关系
 */
@Component
@Slf4j
public class RabbitMqInitializer {

    private final RabbitAdmin rabbitAdmin;
    private final RabbitMqProperty rabbitMqProperty;

    public RabbitMqInitializer(ConnectionFactory connectionFactory, RabbitMqProperty rabbitMqProperty) {
        this.rabbitAdmin = new RabbitAdmin(connectionFactory);
        this.rabbitMqProperty = rabbitMqProperty;
    }

    /**
     * 应用程序启动时初始化RabbitMQ组件
     */
    @PostConstruct
    public void initialize() {
        // 主动创建交换机
        DirectExchange exchange = new DirectExchange(rabbitMqProperty.getExchangeName(), true, false);
        rabbitAdmin.declareExchange(exchange);

        // 主动创建队列
        Queue queue = QueueBuilder.durable(rabbitMqProperty.getQueueName()).withArgument("x-message-ttl", 60000)
            .withArgument("x-ha-policy", "all").build();
        rabbitAdmin.declareQueue(queue);

        // 主动创建绑定关系
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(rabbitMqProperty.getRoutingKey());
        rabbitAdmin.declareBinding(binding);

        log.info("RabbitMQ组件已通过代码初始化完成:");
        log.info("- 交换机: {}", rabbitMqProperty.getExchangeName());
        log.info("- 队列: {}", rabbitMqProperty.getQueueName());
        log.info("- 路由键: {}", rabbitMqProperty.getRoutingKey());
        log.info("- 节点ID: {}", rabbitMqProperty.getNodeId());
    }

    /**
     * 动态创建交换机
     *
     * @param exchangeName 交换机名称
     */
    public void createExchange(String exchangeName) {
        DirectExchange exchange = new DirectExchange(exchangeName, true, false);
        rabbitAdmin.declareExchange(exchange);
    }

    /**
     * 动态创建队列
     *
     * @param queueName 队列名称
     */
    public void createQueue(String queueName) {
        Queue queue =
            QueueBuilder.durable(queueName).withArgument("x-message-ttl", 60000).withArgument("x-ha-policy", "all")
                .build();
        rabbitAdmin.declareQueue(queue);
    }

    /**
     * 动态创建绑定关系
     *
     * @param exchangeName 交换机名称
     * @param queueName    队列名称
     * @param routingKey   路由键
     */
    public void createBinding(String exchangeName, String queueName, String routingKey) {
        Binding binding =
            BindingBuilder.bind(new Queue(queueName)).to(new DirectExchange(exchangeName)).with(routingKey);
        rabbitAdmin.declareBinding(binding);
    }
}
