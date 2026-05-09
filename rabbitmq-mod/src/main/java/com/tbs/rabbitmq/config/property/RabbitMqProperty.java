package com.tbs.rabbitmq.property;

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

}
