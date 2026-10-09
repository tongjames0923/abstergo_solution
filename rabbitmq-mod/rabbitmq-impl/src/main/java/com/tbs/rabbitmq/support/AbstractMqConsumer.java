package com.tbs.rabbitmq.support;

import com.rabbitmq.client.Channel;
import com.tbs.common.util.base.StrUtil;
import com.tbs.mq.api.consumer.AbstractMessageDispatcher;
import com.tbs.mq.api.consumer.IMqConsumerLifecycle;
import com.tbs.mq.api.idempotent.IIdempotentStore;
import com.tbs.mq.api.producer.IMqProducer;
import com.tbs.mq.api.retry.IFailedMessageStore;
import com.tbs.mq.api.retry.IRetryDelayPolicy;
import com.tbs.mq.constants.MqHeaderConstants;
import com.tbs.mq.impl.SteppedRetryDelayPolicy;
import com.tbs.mq.model.DispatchOutcome;
import com.tbs.mq.model.FailedMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 高度可配置的RabbitMQ消费者抽象类
 * <p>
 * 继承{@link AbstractMessageDispatcher}复用幂等占位、异常分类等分派编排，
 * 本类负责对接RabbitMQ容器并落地"分派结果→确认/重试/死信"的路由策略：
 * <ul>
 *     <li>SUCCESS / DUPLICATE_SKIPPED — 直接ACK</li>
 *     <li>BUSINESS_DISCARDED — 按配置转入死信留痕或丢弃，ACK</li>
 *     <li>DEAD_LETTER — 转入死信留痕，触发{@link #handleDeadLetter(Message, Throwable)}钩子，ACK</li>
 *     <li>RETRY_SCHEDULED — 未超重试上限时持久化到失败存储等待调度重投递；
 *         超限或入库失败时降级转入死信，ACK</li>
 * </ul>
 * 全程不再使用Broker原生requeue与x-death头，重试状态由自定义消息头显式维护。
 * </p>
 * <p>
 * 子类只需实现两个方法即可快速创建消费者：
 * <ul>
 *     <li>{@link #getProperties()} — 提供消费者配置</li>
 *     <li>{@link #handleMessage(Object)} — 定义消息处理逻辑</li>
 * </ul>
 * </p>
 *
 * @param <T> 消息体类型
 * @author tongj
 * @see MqConsumerProperties
 * @see MqResourceDeclarer
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractMqConsumer<T> extends AbstractMessageDispatcher<T>
    implements ChannelAwareMessageListener, IMqConsumerLifecycle {

    @Resource
    protected ConnectionFactory connectionFactory;

    @Resource
    protected AmqpTemplate amqpTemplate;

    @Resource
    private MessageConverter messageConverter;

    @Autowired(required = false)
    @Lazy
    private AmqpAdmin amqpAdmin;

    /**
     * 幂等存储（可选），由redis-mod等实现模块提供
     */
    @Autowired(required = false)
    private IIdempotentStore idempotentStoreBean;

    /**
     * 失败消息存储（可选），未提供时重试消息无法持久化
     */
    @Autowired(required = false)
    private IFailedMessageStore failedMessageStoreBean;

    /**
     * 容器级默认重试延迟策略（可选）
     */
    @Autowired(required = false)
    private IRetryDelayPolicy defaultRetryDelayPolicy;

    @Autowired
    private IMqProducer mqProducer;

    private SimpleMessageListenerContainer container;

    /**
     * 运行状态守卫，保证start/stop幂等
     */
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * 当前线程正在处理的原始消息，供幂等键兜底解析使用
     */
    private final ThreadLocal<Message> rawMessageHolder = new ThreadLocal<>();

    /**
     * 当前线程最近一次分派失败的异常，供死信钩子与失败入库使用
     */
    private final ThreadLocal<Throwable> lastErrorHolder = new ThreadLocal<>();

    /**
     * 当前实例生效的重试延迟策略
     */
    private IRetryDelayPolicy effectiveRetryDelayPolicy;

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
     * 抛出异常的类型直接决定后续处理策略，参见{@link com.tbs.mq.api.exception.MqConsumeException}。
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
     * 死信留痕钩子
     * <p>
     * 消息成功转入死信队列后触发，默认记录错误日志。
     * 子类可覆盖实现告警、人工处理台账等扩展逻辑。
     * </p>
     *
     * @param message 进入死信的原始消息
     * @param error   导致死信的异常，可能为null
     */
    protected void handleDeadLetter(Message message, Throwable error) {
        MqConsumerProperties props = getProperties();
        log.error("消息进入死信队列, queue={}, dlq={}, messageId={}", props.getQueueName(),
            MqResourceDeclarer.resolveDeadLetterQueue(props), message.getMessageProperties().getMessageId(), error);
    }

    /**
     * 解析业务幂等键
     * <p>
     * 幂等键来源优先级：本方法提供的业务键 → 消息messageId → 消息内容摘要。
     * 默认返回null，即退化为messageId兜底。
     * </p>
     *
     * @param message 消息体
     * @return 业务幂等键，null表示使用兜底键
     */
    protected String resolveBusinessIdempotentKey(T message) {
        return null;
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
     * 装配分派依赖 → 自动声明资源（如果启用）→ 创建容器 → 启动消费者
     * </p>
     */
    @PostConstruct
    public void init() {
        MqConsumerProperties props = getProperties();

        wireDispatcher(props);

        if (props.isAutoDeclare()) {
            MqResourceDeclarer.declare(amqpAdmin, props);
        }

        container = createContainer();

        if (props.isAutoStart()) {
            start();
        }
    }

    /**
     * 销毁消费者容器
     */
    @PreDestroy
    public void destroy() {
        stop();
    }

    /**
     * 手动启动消费者（幂等）
     */
    @Override
    public final void start() {
        if (running.compareAndSet(false, true)) {
            if (container != null && !container.isRunning()) {
                container.start();
            }
            log.info("RabbitMQ消费者已启动, queue={}, concurrency={}",
                getProperties().getQueueName(), getProperties().getConcurrency());
        }
    }

    /**
     * 手动停止消费者（幂等）
     */
    @Override
    public final void stop() {
        if (running.compareAndSet(true, false)) {
            if (container != null && container.isRunning()) {
                container.stop();
            }
            log.info("RabbitMQ消费者已停止, queue={}", getProperties().getQueueName());
        }
    }

    /**
     * 检查消费者是否正在运行
     *
     * @return true表示正在运行
     */
    @Override
    public boolean isRunning() {
        return running.get();
    }

    // ======================== 消息消费核心 ========================

    /**
     * 处理接收到的RabbitMQ消息
     * <p>
     * 交给分派模板完成幂等与异常分类，再按分派结果路由重试/死信，最终一律ACK。
     * </p>
     *
     * @param message 原始消息对象
     * @param channel 消息通道，用于ACK操作
     */
    @Override
    public void onMessage(Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            rawMessageHolder.set(message);
            T payload = deserialize(message);
            DispatchOutcome outcome = execute(payload);
            handleOutcome(message, outcome);
        } catch (Exception e) {
            // 编排外的基础异常（如反序列化失败）：毒消息直接转入死信留痕，避免反复投递
            log.error("消息消费异常, deliveryTag={}, queue={}", deliveryTag, getProperties().getQueueName(), e);
            routeToDeadLetter(message, e);
        } finally {
            rawMessageHolder.remove();
            lastErrorHolder.remove();
            ackSilently(channel, deliveryTag);
        }
    }

    /**
     * 业务分派实现：委托子类的{@link #handleMessage(Object)}
     *
     * @param message 反序列化后的消息体
     * @throws Exception 处理异常，异常类型即处理策略
     */
    @Override
    public final void dispatch(T message) throws Exception {
        handleMessage(message);
    }

    /**
     * 失败回调：留存异常供后续路由使用，并触发子类回调
     *
     * @param message 处理失败的消息体
     * @param error   异常信息
     */
    @Override
    public void onFailure(T message, Throwable error) {
        lastErrorHolder.set(error);
        handleMessageFailure(message, error);
    }

    /**
     * 解析幂等键
     * <p>
     * 业务键 → 消息messageId → 消息内容摘要的三级兜底，统一附加队列名前缀避免跨队列碰撞。
     * 幂等未开启时返回null跳过幂等控制。
     * </p>
     *
     * @param message 消息体
     * @return 幂等键，null表示跳过幂等
     */
    @Override
    public String resolveIdempotentKey(T message) {
        MqConsumerProperties props = getProperties();
        if (!props.isIdempotentEnabled()) {
            return null;
        }
        String queueName = props.getQueueName();
        String businessKey = resolveBusinessIdempotentKey(message);
        if (StrUtil.isNotEmpty(businessKey)) {
            return queueName + ":" + businessKey;
        }
        Message raw = rawMessageHolder.get();
        if (raw == null) {
            return null;
        }
        String messageId = raw.getMessageProperties().getMessageId();
        if (StrUtil.isNotEmpty(messageId)) {
            return queueName + ":" + messageId;
        }
        return queueName + ":" + digest(raw.getBody());
    }

    /**
     * 按分派结果路由后续动作
     *
     * @param message 原始消息
     * @param outcome 分派结果
     */
    private void handleOutcome(Message message, DispatchOutcome outcome) {
        MqConsumerProperties props = getProperties();
        Throwable error = lastErrorHolder.get();
        switch (outcome) {
            case SUCCESS:
            case DUPLICATE_SKIPPED:
                break;
            case BUSINESS_DISCARDED:
                if (props.isDeadLetterOnBusinessError()) {
                    routeToDeadLetter(message, error);
                } else {
                    log.warn("业务性失败且未开启业务死信, 丢弃消息, queue={}", props.getQueueName());
                }
                break;
            case DEAD_LETTER:
                routeToDeadLetter(message, error);
                break;
            case RETRY_SCHEDULED:
                scheduleRetry(message, error);
                break;
            default:
                break;
        }
    }

    /**
     * 可重试失败的入库排期
     * <p>
     * 未启用重试或超过最大重试次数时转入死信；入库失败同样降级转入死信，保证消息不静默丢失。
     * </p>
     *
     * @param message 原始消息
     * @param error   失败异常
     */
    private void scheduleRetry(Message message, Throwable error) {
        MqConsumerProperties props = getProperties();
        if (!props.isRetryEnabled()) {
            log.warn("未启用重试, 丢弃消息, queue={}", props.getQueueName());
            return;
        }

        int retryCount = readRetryCount(message);
        if (retryCount >= props.getMaxRetryCount()) {
            log.warn("消息超过最大重试次数({}), 转入死信, queue={}", props.getMaxRetryCount(), props.getQueueName());
            routeToDeadLetter(message, error);
            return;
        }

        IFailedMessageStore store = getFailedMessageStore();
        if (store == null) {
            log.warn("失败消息存储未注入, 无法持久化重试消息, 丢弃, queue={}", props.getQueueName());
            return;
        }

        try {
            store.save(buildFailedMessage(message, error, retryCount));
        } catch (Exception saveError) {
            log.error("失败消息入库异常, 降级转入死信, queue={}", props.getQueueName(), saveError);
            routeToDeadLetter(message, error);
        }
    }

    /**
     * 转入死信留痕
     * <p>
     * 保留原消息体、消息头与messageId发布到死信交换机；死信未启用时仅记录日志。
     * </p>
     *
     * @param message 原始消息
     * @param error   导致死信的异常，可能为null
     */
    private void routeToDeadLetter(Message message, Throwable error) {
        MqConsumerProperties props = getProperties();
        String deadLetterExchange = MqResourceDeclarer.resolveDeadLetterExchange(props);
        if (deadLetterExchange == null) {
            log.warn("未启用死信留痕, 丢弃消息, queue={}", props.getQueueName());
            return;
        }
        try {
            amqpTemplate.send(deadLetterExchange, "", copyForDeadLetter(message));
            handleDeadLetter(message, error);
        } catch (Exception e) {
            log.error("发布死信队列失败, queue={}, dlx={}", props.getQueueName(), deadLetterExchange, e);
        }
    }

    /**
     * 构建失败消息快照
     *
     * @param message    原始消息
     * @param error      失败异常
     * @param retryCount 已重试次数
     * @return 可持久化的失败消息记录
     */
    private FailedMessage buildFailedMessage(Message message, Throwable error, int retryCount) {
        MqConsumerProperties props = getProperties();
        MessageProperties source = message.getMessageProperties();
        Date now = new Date();

        FailedMessage failedMessage = new FailedMessage();
        failedMessage.setId(UUID.randomUUID().toString().replace("-", ""));
        failedMessage.setQueueName(props.getQueueName());
        failedMessage.setExchangeName(props.getExchangeName());
        failedMessage.setRoutingKey(props.getRoutingKey());
        failedMessage.setBody(message.getBody());
        failedMessage.setContentType(source.getContentType());

        Map<String, Object> headers = new HashMap<>(source.getHeaders());
        headers.putIfAbsent(MqHeaderConstants.FIRST_FAIL_TIME, now.getTime());
        failedMessage.setHeaders(headers);

        failedMessage.setMessageId(source.getMessageId());
        failedMessage.setRetryCount(retryCount);
        failedMessage.setFirstFailTime(readFirstFailTime(headers, now));
        failedMessage.setErrorMessage(truncate(error == null ? null : String.valueOf(error.getMessage())));

        Duration delay = effectiveRetryDelayPolicy.nextDelay(retryCount);
        failedMessage.setNextRetryTime(Date.from(Instant.now().plus(delay)));
        return failedMessage;
    }

    /**
     * 复制原始消息用于死信发布（保留消息头、messageId与内容类型）
     *
     * @param message 原始消息
     * @return 死信消息副本
     */
    private Message copyForDeadLetter(Message message) {
        MessageProperties source = message.getMessageProperties();
        MessageProperties copy = new MessageProperties();
        copy.getHeaders().putAll(source.getHeaders());
        copy.setMessageId(source.getMessageId());
        copy.setContentType(source.getContentType());
        copy.setContentEncoding(source.getContentEncoding());
        copy.setType(source.getType());
        return new Message(message.getBody(), copy);
    }

    /**
     * 读取消息的已重试次数（自定义头x-retry-count）
     *
     * @param message 原始消息
     * @return 已重试次数，无该头时为0
     */
    protected int readRetryCount(Message message) {
        Object value = message.getMessageProperties().getHeaders().get(MqHeaderConstants.RETRY_COUNT);
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }

    /**
     * 读取首次失败时间，消息头缺失时取当前时间
     *
     * @param headers  消息头
     * @param fallback 兜底时间
     * @return 首次失败时间
     */
    private Date readFirstFailTime(Map<String, Object> headers, Date fallback) {
        Object value = headers.get(MqHeaderConstants.FIRST_FAIL_TIME);
        return value instanceof Number ? new Date(((Number) value).longValue()) : fallback;
    }

    /**
     * 截断错误摘要，避免失败记录过大
     *
     * @param errorMessage 错误信息
     * @return 截断后的错误摘要
     */
    private String truncate(String errorMessage) {
        if (errorMessage == null) {
            return null;
        }
        return errorMessage.length() > 500 ? errorMessage.substring(0, 500) : errorMessage;
    }

    /**
     * 消息体摘要（MD5），幂等键的最终兜底
     *
     * @param body 消息体字节
     * @return 摘要字符串
     */
    private String digest(byte[] body) {
        if (body == null || body.length == 0) {
            return "empty";
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] bytes = messageDigest.digest(body);
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(body.length);
        }
    }

    /**
     * 静默确认消息（ACK失败仅记录日志）
     *
     * @param channel     消息通道
     * @param deliveryTag 投递标签
     */
    protected void ackSilently(Channel channel, long deliveryTag) {
        try {
            channel.basicAck(deliveryTag, false);
        } catch (Exception ackEx) {
            log.error("basicAck执行失败, deliveryTag={}, queue={}",
                deliveryTag, getProperties().getQueueName(), ackEx);
        }
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
        SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer(connectionFactory);
        listenerContainer.setQueueNames(props.getQueueName());
        listenerContainer.setMessageListener(this);
        listenerContainer.setAcknowledgeMode(props.getAcknowledgeMode());
        listenerContainer.setConcurrency(props.getConcurrency());

        int prefetch = props.getPrefetchCount();
        if (prefetch > 0) {
            listenerContainer.setPrefetchCount(prefetch);
        }

        return listenerContainer;
    }

    // ======================== 分派依赖装配 ========================

    /**
     * 装配分派模板所需的幂等、失败存储与延迟策略依赖
     *
     * @param props 消费者配置
     */
    private void wireDispatcher(MqConsumerProperties props) {
        this.effectiveRetryDelayPolicy = props.getRetryDelayPolicy() != null ? props.getRetryDelayPolicy()
            : (defaultRetryDelayPolicy != null ? defaultRetryDelayPolicy : new SteppedRetryDelayPolicy());
        retryDelayPolicy(effectiveRetryDelayPolicy);
        failedMessageStore(failedMessageStoreBean);

        if (props.isIdempotentEnabled()) {
            if (idempotentStoreBean == null) {
                log.warn("已开启消费幂等但容器中不存在IIdempotentStore实现, 幂等降级为关闭, queue={}",
                    props.getQueueName());
            } else {
                idempotentStore(idempotentStoreBean);
                idempotentTtl(resolveIdempotentTtl(props));
            }
        }
    }

    /**
     * 解析幂等键TTL
     * <p>
     * 未显式配置时取最大重试窗口（各档位延迟之和）的3倍。
     * </p>
     *
     * @param props 消费者配置
     * @return 幂等键有效期
     */
    private Duration resolveIdempotentTtl(MqConsumerProperties props) {
        if (props.getIdempotentTtl() != null) {
            return props.getIdempotentTtl();
        }
        Duration retryWindow = Duration.ZERO;
        for (int i = 0; i < props.getMaxRetryCount(); i++) {
            retryWindow = retryWindow.plus(effectiveRetryDelayPolicy.nextDelay(i));
        }
        return retryWindow.multipliedBy(3);
    }

    // ======================== 消息发送 ========================

    /**
     * 发送消息到当前消费者配置的交换机和路由键
     *
     * @param message 要发送的消息体
     * @deprecated 请改用统一的{@link IMqProducer}发送出口，以保证messageId自动补齐
     */
    @Deprecated
    public void send(T message) {
        MqConsumerProperties props = getProperties();
        mqProducer.send(props.getExchangeName(), props.getRoutingKey(), message);
    }

    /**
     * 发送消息到指定的交换机和路由键
     *
     * @param exchangeName 交换机名称
     * @param routingKey   路由键
     * @param message      要发送的消息体
     * @deprecated 请改用统一的{@link IMqProducer}发送出口，以保证messageId自动补齐
     */
    @Deprecated
    public void send(String exchangeName, String routingKey, T message) {
        mqProducer.send(exchangeName, routingKey, message);
    }

}
