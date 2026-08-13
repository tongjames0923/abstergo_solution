package com.tbs.rabbitmq.support;

import com.rabbitmq.client.Channel;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.error.IExceptionLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 高度可配置的RabbitMQ消费者抽象类
 * <p>
 * 子类只需实现两个方法即可快速创建消费者：
 * <ul>
 *     <li>{@link #getProperties()} — 提供消费者配置</li>
 *     <li>{@link #handleMessage(Object)} — 定义消息处理逻辑</li>
 * </ul>
 * 配置属性、资源声明、重试策略等均由独立组件负责，本类仅关注核心消费逻辑和生命周期管理。
 * </p>
 *
 * @param <T> 消息体类型
 * @author tongj
 * @see MqConsumerProperties
 * @see MqResourceDeclarer
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractMqConsumer<T> implements ChannelAwareMessageListener {

    @Resource
    protected ConnectionFactory connectionFactory;

    @Resource
    protected AmqpTemplate amqpTemplate;

    @Resource
    private MessageConverter messageConverter;

    @Autowired(required = false)
    @Lazy
    private AmqpAdmin amqpAdmin;

    private SimpleMessageListenerContainer container;

    // ======================== 子类必须实现 ========================

    /**
     * 获取消费者配置
     * <p>
     * 子类必须实现此方法，返回消费者的所有配置属性。
     * 建议通过 {@link MqConsumerProperties#builder(String)} 构建。
     * </p>
     *
     * @return 消费者配置，不能为空
     */
    protected abstract MqConsumerProperties getProperties();

    /**
     * 处理接收到的消息
     * <p>
     * 子类必须实现此方法，定义具体的消息处理逻辑。
     * 处理成功后基类自动ACK；抛出异常时基类根据重试策略NACK或ACK。
     * </p>
     *
     * @param message 消息体
     * @throws Exception 处理消息时可能发生的异常
     */
    protected abstract void handleMessage(T message) throws Exception;

    // ======================== 可选扩展 ========================

    /**
     * 消息处理失败时的回调
     * <p>
     * 默认仅记录错误日志。子类可覆盖实现自定义错误处理（告警、持久化等）。
     * </p>
     *
     * @param message 处理失败的消息体
     * @param error   异常信息
     */
    protected void handleMessageFailure(T message, Throwable error) {
        log.error("消息处理失败, queue={}", getProperties().getQueueName(), error);
    }

    /**
     * 反序列化消息
     *
     * @param message 原始RabbitMQ消息
     * @return 反序列化后的消息体
     */
    @SuppressWarnings("unchecked")
    protected T deserialize(Message message) {
        return (T) messageConverter.fromMessage(message);
    }

    // ======================== 生命周期 ========================

    /**
     * 初始化消费者
     * <p>
     * 自动声明资源（如果启用）→ 创建容器 → 启动消费者
     * </p>
     */
    @PostConstruct
    public void init() {
        MqConsumerProperties props = getProperties();

        if (props.isAutoDeclare()) {
            MqResourceDeclarer.declare(amqpAdmin, props);
        }

        container = createContainer();

        if (props.isAutoStart()) {
            container.start();
            log.info("RabbitMQ消费者已启动, queue={}, concurrency={}",
                props.getQueueName(), props.getConcurrency());
        }
    }

    /**
     * 销毁消费者容器
     */
    @PreDestroy
    public void destroy() {
        if (container != null && container.isRunning()) {
            container.stop();
            log.info("RabbitMQ消费者已停止, queue={}", getProperties().getQueueName());
        }
    }

    /**
     * 手动启动消费者
     */
    public void start() {
        if (container != null && !container.isRunning()) {
            container.start();
            log.info("RabbitMQ消费者已手动启动, queue={}", getProperties().getQueueName());
        }
    }

    /**
     * 手动停止消费者
     */
    public void stop() {
        if (container != null && container.isRunning()) {
            container.stop();
            log.info("RabbitMQ消费者已手动停止, queue={}", getProperties().getQueueName());
        }
    }

    /**
     * 检查消费者是否正在运行
     *
     * @return true表示正在运行
     */
    public boolean isRunning() {
        return container != null && container.isRunning();
    }

    // ======================== 消息消费核心 ========================

    /**
     * 处理接收到的RabbitMQ消息
     *
     * @param message 原始消息对象
     * @param channel 消息通道，用于ACK/NACK操作
     * @throws Exception 处理消息时可能发生的异常
     */
    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            T obj = deserialize(message);
            handleMessage(obj);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            String queueName = getProperties().getQueueName();
            log.error("消息处理失败, deliveryTag={}, queue={}", deliveryTag, queueName, e);
            handleError(message, channel, deliveryTag, e);
        }
    }

    /**
     * 异常处理：调用回调 → 根据重试策略决定ACK或NACK
     *
     * @param message     原始消息
     * @param channel     消息通道
     * @param deliveryTag 投递标签
     * @param error       异常信息
     */
    protected void handleError(Message message, Channel channel, long deliveryTag, Exception error) {
        invokeFailureCallback(message, error);

        // 业务异常直接ACK，不重试（重试无法解决问题）
        if (isBusinessException(error)) {
            log.warn("业务异常，丢弃消息, deliveryTag={}, queue={}, error={}",
                deliveryTag, getProperties().getQueueName(), error.getMessage());
            ackSilently(channel, deliveryTag);
            return;
        }

        MqConsumerProperties props = getProperties();
        if (!props.isRetryEnabled()) {
            ackSilently(channel, deliveryTag);
            return;
        }

        try {
            int retryCount = getRetryCount(message);
            if (retryCount >= props.getMaxRetryCount()) {
                log.warn("消息超过最大重试次数({}), 丢弃消息, deliveryTag={}, queue={}",
                    props.getMaxRetryCount(), deliveryTag, props.getQueueName());
                channel.basicNack(deliveryTag, false, false);
            } else {
                log.debug("消息重新入队, retryCount={}, deliveryTag={}, queue={}",
                    retryCount, deliveryTag, props.getQueueName());
                channel.basicNack(deliveryTag, false, true);
            }
        } catch (IOException nackEx) {
            log.error("basicNack执行失败, deliveryTag={}, queue={}",
                deliveryTag, props.getQueueName(), nackEx);
            ackSilently(channel, deliveryTag);
        }
    }

    /**
     * 调用错误处理回调，异常安全
     */
    private void invokeFailureCallback(Message message, Exception error) {
        try {
            T obj = deserialize(message);
            handleMessageFailure(obj, error);
        } catch (Exception callbackEx) {
            log.error("错误处理回调执行失败, queue={}", getProperties().getQueueName(), callbackEx);
        }
    }

    /**
     * 判断是否为业务异常
     * <p>
     * 业务异常（用户输入错误、业务逻辑错误）不应重试，因为重试无法解决问题。
     * 系统异常（网络、数据库等临时故障）应启用重试机制。
     * </p>
     *
     * @param error 异常信息
     * @return true表示业务异常，false表示系统异常
     */
    protected boolean isBusinessException(Exception error) {
        if (error instanceof IExceptionLevel) {
            ErrorLevelEnum level = ((IExceptionLevel) error).getLevel();
            return level == ErrorLevelEnum.USER_INPUT_ERROR
                || level == ErrorLevelEnum.BUSINESS_LOGIC_ERROR;
        }
        return false;
    }

    /**
     * 静默确认消息（NACK失败时的降级策略）
     */
    protected void ackSilently(Channel channel, long deliveryTag) {
        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException ackEx) {
            log.error("basicAck降级执行失败, deliveryTag={}, queue={}",
                deliveryTag, getProperties().getQueueName(), ackEx);
        }
    }

    /**
     * 获取消息的重试次数
     * <p>
     * 通过RabbitMQ的x-death头信息判断已被拒绝重入队的次数。
     * </p>
     *
     * @param message 原始消息
     * @return 重试次数
     */
    @SuppressWarnings("unchecked")
    protected int getRetryCount(Message message) {
        Object xDeath = message.getMessageProperties().getHeaders().get("x-death");
        if (xDeath instanceof List) {
            List<Object> deathList = (List<Object>) xDeath;
            int count = 0;
            for (Object death : deathList) {
                if (death instanceof Map) {
                    Map<String, Object> deathInfo = (Map<String, Object>) death;
                    Object countObj = deathInfo.get("count");
                    if (countObj instanceof Number) {
                        count += ((Number) countObj).intValue();
                    }
                }
            }
            return count;
        }
        return 0;
    }

    // ======================== 容器创建 ========================

    /**
     * 创建消息监听器容器
     * <p>
     * 根据配置创建SimpleMessageListenerContainer。子类可覆盖实现自定义容器配置。
     * </p>
     *
     * @return 配置好的消息监听器容器
     */
    protected SimpleMessageListenerContainer createContainer() {
        MqConsumerProperties props = getProperties();
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueueNames(props.getQueueName());
        container.setMessageListener(this);
        container.setAcknowledgeMode(props.getAcknowledgeMode());
        container.setConcurrency(props.getConcurrency());

        int prefetch = props.getPrefetchCount();
        if (prefetch > 0) {
            container.setPrefetchCount(prefetch);
        }

        return container;
    }

    // ======================== 消息发送 ========================

    /**
     * 发送消息到当前消费者配置的交换机和路由键
     *
     * @param message 要发送的消息体
     */
    public void send( T message) {
        MqConsumerProperties props = getProperties();
        amqpTemplate.convertAndSend(props.getExchangeName(), props.getRoutingKey(), message);
    }

    /**
     * 发送消息到指定的交换机和路由键
     *
     * @param exchangeName 交换机名称
     * @param routingKey   路由键
     * @param message      要发送的消息体
     */
    public void send( String exchangeName,  String routingKey,  T message) {
        amqpTemplate.convertAndSend(exchangeName, routingKey, message);
    }

}
