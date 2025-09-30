package com.tbs.broadcast.rabbitmq.config;

import com.tbs.broadcast.rabbitmq.config.property.RabbitMqProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SerializerMessageConverter;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Slf4j
@Configuration
public class RabbitMqConfig {

	@Resource
	private RabbitMqProperty rabbitMqProperty;

	@Resource
	private RabbitProperties rabbitProperties;

	/**
	 * 创建连接工厂
	 *
	 * @return ConnectionFactory实例
	 */
	@Bean
	public ConnectionFactory connectionFactory() {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory();

		// 从Spring Boot配置中获取RabbitMQ连接信息
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
        // 增加重试机制
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
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setMessageConverter(messageConverter);
		template.setConfirmCallback((correlationData, ack, cause) -> {
			if (!ack) {
				System.err.println("消息发送失败: " + cause);
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
    public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

	/**
	 * 创建JSON消息转换器
	 *
	 * @return MessageConverter实例
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
	 * 创建广播队列
	 *
	 * @return Queue实例
	 */
	@Bean
	public Queue broadcastQueue() {
        // 使用更宽松的队列声明方式，避免声明冲突
        return QueueBuilder.durable(rabbitMqProperty.getQueueName()).withArgument("x-message-ttl", 60000) // 设置消息TTL为60秒
            .withArgument("x-ha-policy", "all") // 启用高可用策略
            .build();
    }

    /**
     * 在RabbitMQ中声明队列，确保其存在
     *
     * @param amqpAdmin AMQP管理器
     * @param queue     队列
     */
    @Bean
    public Object declareQueue(AmqpAdmin amqpAdmin, Queue queue) {
        return new Object() {
            @PostConstruct
            public void initialize() {
                try {
                    amqpAdmin.declareQueue(queue);
                    log.info("队列已存在: " + queue.getName());
                } catch (Exception e) {
                    log.error("队列声明失败: " + e.getMessage());
                }
            }
        };
	}

	/**
	 * 绑定队列到交换机
	 *
	 * @return Binding实例
	 */
	@Bean
	public Binding broadcastBinding() {
		return BindingBuilder.bind(broadcastQueue()).to(broadcastExchange()).with(rabbitMqProperty.getRoutingKey());
	}

}
