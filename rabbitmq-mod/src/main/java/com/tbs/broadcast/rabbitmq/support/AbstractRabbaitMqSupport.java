package com.tbs.broadcast.rabbitmq.support;

import com.rabbitmq.client.Channel;
import com.tbs.broadcast.bus.IDistributedBusSupport;
import com.tbs.broadcast.dispatcher.IEventDispatcher;
import com.tbs.broadcast.support.AbstractDistributedSupport;
import com.tbs.rabbitmq.property.RabbitMqProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Lazy;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

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
@AutoConfigureAfter({IEventDispatcher.class})
public abstract class AbstractRabbaitMqSupport<T> extends AbstractDistributedSupport<T>
    implements ChannelAwareMessageListener {

    @Resource
    protected RabbitMqProperty property;

    @Resource
    private AmqpTemplate amqpTemplate;

    @Resource
    private AmqpAdmin amqpAdmin;

    @Resource
    private MessageConverter messageConverter;

    @Resource
    private ConnectionFactory connectionFactory;

    private SimpleMessageListenerContainer container;

    @Resource
    @Lazy
    private IEventDispatcher dispatcher;

    /**
     * 构造方法
     * <p>
     * 创建AbstractRabbaitMqSupport实例
     * </p>
     */
    public AbstractRabbaitMqSupport() {
        log.info("AbstractRabbaitMqSupport init");
    }

    /**
     * 初始化方法
     * <p>
     * 在应用启动时执行初始化操作，包括：
     * 1. 确保队列存在
     * 2. 创建并配置消息监听器容器
     * 3. 启动消息监听器
     * </p>
     */
    @PostConstruct
    public void init() {
        // 确保队列存在
        declareQueueIfNeeded();

        container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueueNames(property.getQueueName());
        container.setMessageListener(this);
        // 设置为手动ACK
        container.setAcknowledgeMode(org.springframework.amqp.core.AcknowledgeMode.MANUAL);
        container.start();
    }

    /**
     * 确保队列存在，如果不存在则创建
     * <p>
     * 尝试声明队列，如果队列已存在则忽略异常
     * </p>
     */
    private void declareQueueIfNeeded() {
        try {
            // 尝试声明队列
            org.springframework.amqp.core.Queue queue =
                new org.springframework.amqp.core.Queue(property.getQueueName(), true);
            amqpAdmin.declareQueue(queue);
        } catch (Exception e) {
            log.warn("队列声明失败，可能已存在: {}", e.getMessage());
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
        try {
            T obj = (T)messageConverter.fromMessage(message);
            this.receiveEvent(obj);
            // 处理成功后手动ACK
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("RabbitMqSupport.onMessage error", e);
            // 处理失败后拒绝消息，可以选择是否重新入队
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
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
     * <p>
     * 实现IDistributedBusSupport接口的nodeId方法
     * 从配置属性中获取节点ID
     * </p>
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
