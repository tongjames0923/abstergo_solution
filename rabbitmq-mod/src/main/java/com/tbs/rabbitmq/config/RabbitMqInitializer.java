package com.tbs.rabbitmq.config;

import com.tbs.rabbitmq.property.RabbitMqProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * RabbitMQ初始化器
 * <p>
 * 用于在应用程序启动时通过代码直接创建交换机、队列和绑定关系
 * 提供了静态初始化和动态创建RabbitMQ组件的功能
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
@Component
@Slf4j
public class RabbitMqInitializer {

    private final RabbitAdmin rabbitAdmin;
    private final RabbitMqProperty rabbitMqProperty;

    /**
     * 构造方法
     * <p>
     * 创建RabbitMqInitializer实例
     * </p>
     *
     * @param connectionFactory RabbitMQ连接工厂
     * @param rabbitMqProperty  RabbitMQ配置属性
     */
    public RabbitMqInitializer(ConnectionFactory connectionFactory, RabbitMqProperty rabbitMqProperty) {
        this.rabbitAdmin = new RabbitAdmin(connectionFactory);
        this.rabbitMqProperty = rabbitMqProperty;
    }

    /**
     * 应用程序启动时初始化RabbitMQ组件
     * <p>
     * 在应用启动时创建默认的交换机、队列和绑定关系
     * 使用配置的交换机名称、队列名称和路由键
     * 设置队列的TTL为60秒，并开启高可用策略
     * </p>
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
     * <p>
     * 创建持久化、非自动删除的直接交换机
     * </p>
     *
     * @param exchangeName 交换机名称
     */
    public void createExchange(String exchangeName) {
        DirectExchange exchange = new DirectExchange(exchangeName, true, false);
        rabbitAdmin.declareExchange(exchange);
    }

    /**
     * 动态创建队列
     * <p>
     * 创建持久化队列，设置消息TTL为60秒，并开启高可用策略
     * </p>
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
        rabbitAdmin.declareBinding(binding);
    }
}
