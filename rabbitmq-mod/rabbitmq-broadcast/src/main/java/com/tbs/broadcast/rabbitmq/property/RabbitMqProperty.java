package com.tbs.broadcast.rabbitmq.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ配置属性类
 * <p>
 * 用于绑定tbs.rabbitmq前缀的配置属性，提供RabbitMQ的基本配置信息
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
@ConfigurationProperties("tbs.rabbitmq")
@Data
@Component
public class RabbitMqProperty {
	/**
	 * 交换机名称
	 */
	private String exchangeName = "tbs.broadcast.exchange";

	/**
	 * 队列名称
	 */
	private String queueName = "tbs.broadcast.queue";

	/**
	 * 路由键
	 */
	private String routingKey = "tbs.broadcast.routing.key";

	/**
	 * 节点ID
	 */
	private String nodeId;

	/**
	 * v2消费上下文ID：记录层按此隔离广播队列的消息与台账归属，多广播实例时必须各不相同
	 */
	private Long contextId = 1L;

	/**
	 * 消息TTL（毫秒），默认60秒
	 */
	private long messageTtl = 60000;

	/**
	 * 消费者最大重试次数，超过后消息将被丢弃或路由到死信队列
	 */
	private int maxRetryCount = 3;

	/**
	 * 消费者并发度，格式："min-max"，如 "1-5"，默认 "1-1"
	 */
	private String consumerConcurrency = "1-1";

	/**
	 * 死信交换机名称，为空则不启用死信队列
	 */
	private String deadLetterExchange;

	/**
	 * 死信队列名称
	 */
	private String deadLetterQueue;

}
