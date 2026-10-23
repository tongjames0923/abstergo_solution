package com.tbs.broadcast.rabbitmq.support;

import com.tbs.broadcast.bus.IDistributedBusSupport;
import com.tbs.broadcast.dispatcher.IDistributedDispatcherSupport;
import com.tbs.broadcast.dispatcher.IEventDispatcher;
import com.tbs.broadcast.publisher.IPublishResult;
import com.tbs.common.context.IOperatorContext;
import com.tbs.common.data.IDataBridge;
import com.tbs.broadcast.rabbitmq.property.RabbitMqProperty;
import com.tbs.mq.constants.MqHeaderConstants;
import com.tbs.mq.v2.consume.IConsumerRegistry;
import com.tbs.mq.v2.consume.IMqConsumer;
import com.tbs.mq.v2.producer.IMqMessageProducer;
import com.tbs.mq.v2.vo.MqMessageOutcomeVO;
import com.tbs.rabbitmq.support.MqConsumerProperties;
import com.tbs.rabbitmq.v2.RabbitMqContext;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ResolvableType;

import jakarta.annotation.Resource;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ广播支持抽象类（v2 记录驱动模型）
 * <p>
 * 继承 v2 {@link RabbitMqContext}，复用其自装配监听容器与接收→建档→分发→确认全链路；
 * 自身同时实现 {@link IMqConsumer}，初始化时按负载类型注册进宿主的 {@link IConsumerRegistry}，
 * 消费动作即把负载交回分布式事件系统。广播发送经 v2 统一发送契约 {@link IMqMessageProducer}，
 * messageId 由框架自动补齐。
 * </p>
 * <p>
 * 协作 Bean（recorder/receiver/processor/monopolizer/registry/converter-registry）由宿主应用按
 * {@code MQ v2} 装配惯例提供，负载类型经子类泛型自解析，本类不感知具体事件类型。
 * </p>
 *
 * @param <T> 事件类型（可序列化负载）
 * @author tongj
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractRabbitMqSupport<T extends Serializable> extends RabbitMqContext<T>
    implements IDistributedBusSupport<T>, IDistributedDispatcherSupport, IDataBridge<T, IPublishResult>,
    IMqConsumer<T> {

    @Resource
    protected RabbitMqProperty property;

    /**
     * v2消息发送契约，广播事件的组装与投递由其承担
     */
    @Resource
    private IMqMessageProducer mqMessageProducer;

    /**
     * 当前操作人上下文，由业务模块装配；缺失时不写入操作人头
     */
    @Autowired(required = false)
    private IOperatorContext operatorContext;

    @Resource
    @Lazy
    private IEventDispatcher dispatcher;

    /**
     * v2消费者注册表（宿主装配），本支持类作为消费者按负载类型注册
     */
    @Resource
    private IConsumerRegistry consumerRegistry;

    /**
     * 把自己注册进消费台账链：分发段按负载类型回查消费者后方可收到投递
     */
    @PostConstruct
    public void registerAsConsumer() {
        consumerRegistry.register(this, getMessagePayloadType());
        log.info("[Broadcast] RabbitMQ广播支持已注册为v2消费者, payloadType={}, queue={}",
            getMessagePayloadType().getName(), property.getQueueName());
    }

    /**
     * 获取消费者配置
     * <p>
     * 基于RabbitMqProperty构建MqConsumerProperties，将全局配置适配为实例级配置。
     * </p>
     *
     * @return 消费者配置实例
     */
    @Override
    protected MqConsumerProperties getMqConsumerProperties() {
        return MqConsumerProperties.builder(property.getQueueName()).exchange(property.getExchangeName())
            .routingKey(property.getRoutingKey()).concurrency(property.getConsumerConcurrency())
            .maxRetryCount(property.getMaxRetryCount()).acknowledgeMode(AcknowledgeMode.MANUAL)
            .messageTtl(property.getMessageTtl())
            .deadLetterExchange(property.getDeadLetterExchange())
            .build();
    }

    /**
     * 业务负载类型：从子类绑定的泛型参数自解析（v2 消费者注册与分发回查的路由键）
     *
     * @return 事件类型 Class
     */
    @Override
    @SuppressWarnings("unchecked")
    public Class<T> getMessagePayloadType() {
        return (Class<T>) ResolvableType.forClass(getClass()).as(AbstractRabbitMqSupport.class).getGeneric(0)
            .resolve();
    }

    /**
     * 上下文（队列级）身份标识，记录层按此隔离台账归属
     *
     * @return 配置中的上下文ID
     */
    @Override
    public Long getContextId() {
        return property.getContextId();
    }

    @Override
    public String businessId() {
        return "broadcast";
    }

    @Override
    public Integer consumeId() {
        return 1;
    }

    /**
     * 消费事件：交回分布式事件系统（原 v1 handleMessage 桥接语义）
     *
     * @param message 事件负载
     */
    @Override
    public void consume(T message) {
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
     * 经v2统一发送契约 {@link IMqMessageProducer} 以信封形式发送到配置的交换机与路由键，
     * 由框架自动补齐messageId，负载经转换器携带 payloadType/payloadFormat 头与接收端对称；
     * 当前存在操作人时附带 {@link MqHeaderConstants#OPERATOR_ID} 头，供消费端还原审计主体。
     * </p>
     *
     * @param result 要广播的事件对象
     */
    @Override
    public void broadcastEvent(T result) {
        Map<String, String> headers = new HashMap<>(2);
        if (operatorContext != null) {
            operatorContext.getCurrentOperatorId()
                .ifPresent(operatorId -> headers.put(MqHeaderConstants.OPERATOR_ID, String.valueOf(operatorId)));
        }
        mqMessageProducer.send(MqMessageOutcomeVO.<T>builder().topic(property.getExchangeName())
            .routeKey(property.getRoutingKey()).payload(result).headers(headers).build());
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
