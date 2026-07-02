package com.tbs.broadcast.rabbitmq.support;

import com.rabbitmq.client.Channel;
import com.tbs.broadcast.bus.IDistributedBusSupport;
import com.tbs.broadcast.dispatcher.IEventDispatcher;
import com.tbs.broadcast.support.AbstractDistributedSupport;
import com.tbs.rabbitmq.property.RabbitMqProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Lazy;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.IOException;

/**
 * RabbitMQ广播支持抽象类
 * <p>
 * 继承自AbstractDistributedSupport，实现ChannelAwareMessageListener接口
 * 提供基于RabbitMQ的分布式事件广播和接收功能
 * </p>
 *
 * @param <T> 事件类型
 * @author tongj
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractRabbitMqSupport<T> extends AbstractDistributedSupport<T>
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

    /**
     * 初始化方法
     * <p>
     * 在应用启动时创建并配置消息监听器容器，包括：
     * 1. 创建监听器容器
     * 2. 设置队列、监听器、ACK模式和并发度
     * 3. 启动消息监听器
     * </p>
     */
    @PostConstruct
    public void init() {
        container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueueNames(property.getQueueName());
        container.setMessageListener(this);
        container.setAcknowledgeMode(org.springframework.amqp.core.AcknowledgeMode.MANUAL);
        container.setConcurrency(property.getConsumerConcurrency());
        container.start();
        log.info("RabbitMQ消费者已启动，队列: {}，并发度: {}", property.getQueueName(), property.getConsumerConcurrency());
    }

    /**
     * 应用关闭时停止消息监听器容器
     */
    @PreDestroy
    public void destroy() {
        if (container != null && container.isRunning()) {
            container.stop();
            log.info("RabbitMQ消费者已停止，队列: {}", property.getQueueName());
        }
    }

    /**
     * 处理接收到的消息
     * <p>
     * 实现ChannelAwareMessageListener接口的onMessage方法
     * 负责将接收到的RabbitMQ消息转换为事件对象，并进行处理
     * 使用手动ACK模式确认消息处理结果
     * </p>
     *
     * @param message 接收到的消息
     * @param channel 消息通道
     * @throws Exception 处理消息时可能发生的异常
     */
    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            T obj = (T)messageConverter.fromMessage(message);
            this.receiveEvent(obj);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("RabbitMQ消息处理失败, deliveryTag={}", deliveryTag, e);
            nackMessage(channel, message, deliveryTag);
        }
    }

    /**
     * 拒绝消息处理
     * <p>
     * 根据消息重试次数决定是否重新入队：
     * - 超过最大重试次数时，直接丢弃消息（如果配置了死信队列则路由到死信队列）
     * - 未超过时，重新入队等待重试
     * </p>
     *
     * @param channel     消息通道
     * @param message     消息对象
     * @param deliveryTag 投递标签
     */
    private void nackMessage(Channel channel, Message message, long deliveryTag) {
        try {
            int retryCount = getRetryCount(message);
            if (retryCount >= property.getMaxRetryCount()) {
                log.warn("消息超过最大重试次数({})，丢弃消息, deliveryTag={}", property.getMaxRetryCount(), deliveryTag);
                channel.basicNack(deliveryTag, false, false);
            } else {
                channel.basicNack(deliveryTag, false, true);
            }
        } catch (IOException nackEx) {
            log.error("basicNack执行失败, deliveryTag={}", deliveryTag, nackEx);
            try {
                channel.basicAck(deliveryTag, false);
            } catch (IOException ackEx) {
                log.error("basicAck降级执行失败, deliveryTag={}", deliveryTag, ackEx);
            }
        }
    }

    /**
     * 获取消息的重试次数
     * <p>
     * 通过RabbitMQ的x-death头信息判断消息已被拒绝重入队的次数
     * </p>
     *
     * @param message 消息对象
     * @return 重试次数
     */
    @SuppressWarnings("unchecked")
    private int getRetryCount(Message message) {
        Object xDeath = message.getMessageProperties().getHeaders().get("x-death");
        if (xDeath instanceof java.util.List) {
            java.util.List<Object> deathList = (java.util.List<Object>)xDeath;
            int count = 0;
            for (Object death : deathList) {
                if (death instanceof java.util.Map) {
                    java.util.Map<String, Object> deathInfo = (java.util.Map<String, Object>)death;
                    Object countObj = deathInfo.get("count");
                    if (countObj instanceof Number) {
                        count += ((Number)countObj).intValue();
                    }
                }
            }
            return count;
        }
        return 0;
    }

    /**
     * 广播事件
     * <p>
     * 实现IDistributedBusSupport接口的broadcastEvent方法
     * 使用AMQP模板将事件对象发送到指定的交换机和路由键
     * </p>
     *
     * @param result 要广播的事件对象
     */
    @Override
    public void broadcastEvent(T result) {
        amqpTemplate.convertAndSend(property.getExchangeName(), property.getRoutingKey(), result);
    }

    /**
     * 获取节点ID
     *
     * @return 节点ID
     */
    @Override
    public String nodeId() {
        return property.getNodeId();
    }

    /**
     * 处理事件
     * <p>
     * 实现IDistributedBusSupport接口的handleEvent方法
     * 使用事件分发器处理接收到的事件
     * </p>
     *
     * @param val     事件值
     * @param support 分布式总线支持实例
     * @param <R>     事件类型
     */
    @Override
    public <R> void handleEvent(R val, IDistributedBusSupport<R> support) {
        dispatcher.handleEvent(support.getDataBridge().unbridge(val));
    }

}
