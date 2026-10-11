package com.tbs.broadcast.rabbitmq.support;

import com.tbs.broadcast.bus.IDistributedBusSupport;
import com.tbs.broadcast.dispatcher.IDistributedDispatcherSupport;
import com.tbs.broadcast.dispatcher.IEventDispatcher;
import com.tbs.broadcast.publisher.IPublishResult;
import com.tbs.common.data.IDataBridge;
import com.tbs.broadcast.rabbitmq.property.RabbitMqProperty;
import com.tbs.mq.api.producer.IMqProducer;
import com.tbs.rabbitmq.support.AbstractMqConsumer;
import com.tbs.rabbitmq.support.MqConsumerProperties;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.context.annotation.Lazy;

import jakarta.annotation.Resource;

/**
 * RabbitMQ广播支持抽象类
 * <p>
 * 继承自AbstractMqConsumer，复用其MQ消费基础设施（容器管理、ACK、重试等）。
 * 同时实现IDistributedBusSupport、IDistributedDispatcherSupport和IDataBridge接口，
 * 提供基于RabbitMQ的分布式事件广播和接收功能。
 * </p>
 *
 * @param <T> 事件类型
 * @author tongj
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractRabbitMqSupport<T> extends AbstractMqConsumer<T>
    implements IDistributedBusSupport<T>, IDistributedDispatcherSupport, IDataBridge<T, IPublishResult> {

    @Resource
    protected RabbitMqProperty property;

    @Resource
    private IMqProducer mqProducer;

    @Resource
    @Lazy
    private IEventDispatcher dispatcher;

    /**
     * 获取消费者配置
     * <p>
     * 基于RabbitMqProperty构建MqConsumerProperties，将全局配置适配为实例级配置。
     * </p>
     *
     * @return 消费者配置实例
     */
    @Override
    protected MqConsumerProperties getProperties() {
        return MqConsumerProperties.builder(property.getQueueName()).exchange(property.getExchangeName())
            .routingKey(property.getRoutingKey()).concurrency(property.getConsumerConcurrency())
            .maxRetryCount(property.getMaxRetryCount()).acknowledgeMode(AcknowledgeMode.MANUAL)
            .messageTtl(property.getMessageTtl())
            .deadLetterExchange(property.getDeadLetterExchange())
            .build();
    }

    /**
     * 处理接收到的消息
     * <p>
     * 将MQ消息桥接后交给分布式事件系统处理。
     * </p>
     *
     * @param message 消息体
     * @throws Exception 处理消息时可能发生的异常
     */
    @Override
    protected void handleMessage( T message) throws Exception {
        this.receiveEvent(message);
    }

    /**
     * 接收事件
     * <p>
     * 将接收到的数据交给事件分发器处理。
     * </p>
     *
     * @param data 事件数据
     */
    @Override
    public void receiveEvent(T data) {
        handleEvent(data, this);
    }

    /**
     * 广播事件
     * <p>
     * 经统一发送出口{@link IMqProducer}将事件对象发送到指定的交换机和路由键，
     * 由框架自动补齐messageId。
     * </p>
     *
     * @param result 要广播的事件对象
     */
    @Override
    public void broadcastEvent(T result) {
        mqProducer.send(property.getExchangeName(), property.getRoutingKey(), result);
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
     * 处理分布式事件
     * <p>
     * 使用事件分发器处理接收到的事件，先通过数据桥接器解桥接再分发。
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

    /**
     * 获取数据桥接器
     *
     * @return 当前实例（自身即为数据桥接器）
     */
    @Override
    public IDataBridge<T, IPublishResult> getDataBridge() {
        return this;
    }

}
