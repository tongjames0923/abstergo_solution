package com.tbs.broadcast.rabbitmq.support;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Lazy;

import com.rabbitmq.client.Channel;
import com.tbs.broadcast.bus.IDistributedBusSupport;
import com.tbs.broadcast.dispatcher.IEventDispatcher;
import com.tbs.broadcast.rabbitmq.config.property.RabbitMqProperty;
import com.tbs.broadcast.support.AbstractDistributedSupport;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractRabbaitMqSupport<T> extends AbstractDistributedSupport<T>
		implements ChannelAwareMessageListener {

	@Resource
	protected RabbitMqProperty property;

	@Resource
	private AmqpTemplate amqpTemplate;

	@Resource
	private MessageConverter messageConverter;

	@Resource
	private ConnectionFactory connectionFactory;

	private SimpleMessageListenerContainer container;

	@Resource
	@Lazy
	private IEventDispatcher dispatcher;

	public AbstractRabbaitMqSupport() {
	}

	@PostConstruct
	public void init() {
		container = new SimpleMessageListenerContainer(connectionFactory);
		container.setQueueNames(property.getQueueName());
		container.setMessageListener(this);
		container.setAcknowledgeMode(org.springframework.amqp.core.AcknowledgeMode.MANUAL); // 设置为手动ACK
		container.start();
	}

	@Override
	public void onMessage(Message message, Channel channel) throws Exception {
		try {
			T obj = (T) messageConverter.fromMessage(message);
			this.receiveEvent(obj);
			// 处理成功后手动ACK
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
		} catch (Exception e) {
			log.error("RabbitMqSupport.onMessage error", e);
			// 处理失败后拒绝消息，可以选择是否重新入队
			channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
		}
	}

	@Override
	public void broadcastEvent(T result) {
		amqpTemplate.convertAndSend(property.getExchangeName(), property.getRoutingKey(), result);
	}

	@Override
	public String nodeId() {
		return property.getNodeId();
	}

	@Override
	public <R> void handleEvent(R val, IDistributedBusSupport<R> support) {
		dispatcher.handleEvent(support.getDataBridge().unbridge(val));
	}

}
