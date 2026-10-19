package com.tbs.rabbitmq.v2;

import com.rabbitmq.client.Channel;
import com.tbs.rabbitmq.support.MqConsumerProperties;
import com.tbs.rabbitmq.support.MqResourceDeclarer;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareBatchMessageListener;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * RabbitMQ 消费上下文（v2 记录驱动模型 · 容器推送 / 原生批量监听形态）
 * <p>
 * 与 RabbitMQ Broker 的公共对接（传输层固定 {@link Message}、逐条 {@code basicAck}/{@code basicNack}、
 * {@link ThreadLocal} 通道承载）已上提至 {@link AbstractRabbitMqContext}；本类只负责
 * <b>自装配监听容器</b>这一种接入形态：
 * <ol>
 *     <li>连接装配：{@link #afterPropertiesSet()} 先启动父类处理线程，再按需自动声明资源并拉起
 *     {@link SimpleMessageListenerContainer}，以自身为 {@link ChannelAwareBatchMessageListener} 监听队列；</li>
 *     <li>接收入口：单条推送经 {@link #onMessage(Message, Channel)}、消费端批量经
 *     {@link #onMessageBatch(List, Channel)}（{@link MqConsumerProperties#isConsumerBatchEnabled()} 开启），
 *     二者统一委托父类接收入口做批量建档 + 逐条确认。</li>
 * </ol>
 * 若倾向用 {@code @RabbitListener} 注解接入而非自建容器，改用 {@link RabbitMqListenerContext}。
 * </p>
 * <p>
 * 抽象类，业务子类绑定负载类型 T，提供 {@link #getMqConsumerProperties()}、{@code getMessagePayloadType()}
 * 与 {@code getContextId()}，并声明为 Spring Bean；receiver/processor/recorder/monopolizer 等协作 Bean 由父类按类型注入。
 * </p>
 *
 * @param <T> 业务负载类型（消费者面对的类型）
 * @author tongj
 * @since 2.0.0
 */
@Slf4j
public abstract class RabbitMqContext<T extends Serializable> extends AbstractRabbitMqContext<T>
    implements ChannelAwareBatchMessageListener {

    @Resource
    private ConnectionFactory connectionFactory;

    /**
     * AMQP 资源管理器，由同模块的 TbsRabbitMqConfig 提供，用于 autoDeclare 时声明队列/交换机/绑定
     */
    @Resource
    private AmqpAdmin amqpAdmin;

    /**
     * 消息监听容器，负责从 RabbitMQ 队列拉取投递并回调 {@link #onMessage(Message, Channel)}
     */
    private SimpleMessageListenerContainer container;

    /**
     * 提供 RabbitMQ 传输层配置（队列、交换机、绑定、并发、预取、ack 模式、自动声明/启动等）
     * <p>
     * 建议 {@code acknowledgeMode} 保持默认的 {@link AcknowledgeMode#MANUAL}：v2 以记录为准， 确认动作由框架在建档成功/失败时精确触发，AUTO/AUTO_ACK
     * 模式会绕过该语义。
     * </p>
     *
     * @return 消费者传输配置，不为空
     */
    protected abstract MqConsumerProperties getMqConsumerProperties();

    /**
     * 厂商监听回调入口（单条投递）：委托批量入口，两条路径逻辑完全一致
     *
     * @param message 厂商原始消息
     * @param channel 投递通道，用于后续确认
     */
    @Override
    public void onMessage(Message message, Channel channel) {
        onMessageBatch(Collections.singletonList(message), channel);
    }

    /**
     * 厂商批量监听回调入口（消费端批量）：同一消费线程内暂存通道后，把整批原始消息 交由框架建档编排（批量建档、逐条 ack/nak 均在本线程同步完成）
     *
     * @param messages 厂商原始消息批次
     * @param channel  投递通道，批内消息均来自该通道
     */
    @Override
    public void onMessageBatch(List<Message> messages, Channel channel) {
        ingest(messages, channel);
    }

    /**
     * 启动处理线程后，按需自动声明 MQ 资源并拉起监听容器
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        MqConsumerProperties props = getMqConsumerProperties();
        if (props.isAutoDeclare()) {
            MqResourceDeclarer.declare(amqpAdmin, props);
        }
        container = createContainer(props);
        if (props.isAutoStart()) {
            container.start();
            log.info("RabbitMQ v2 consumer started, queue={}, concurrency={}", props.getQueueName(),
                props.getConcurrency());
        }
    }

    /**
     * 先停止拉取（容器），再停止处理线程（父类），避免停机时丢弃未处理消息
     */
    @Override
    public void destroy() throws Exception {
        if (container != null) {
            container.stop();
        }
        super.destroy();
    }

    /**
     * 创建消息监听容器，子类可覆盖以定制容器参数
     *
     * @param props 传输配置
     * @return 配置好的监听容器
     */
    protected SimpleMessageListenerContainer createContainer(MqConsumerProperties props) {
        SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer(connectionFactory);
        listenerContainer.setQueueNames(props.getQueueName());
        listenerContainer.setMessageListener(this);
        listenerContainer.setAcknowledgeMode(props.getAcknowledgeMode());
        listenerContainer.setConcurrency(props.getConcurrency());
        int prefetch = props.getPrefetchCount();
        if (prefetch > 0) {
            listenerContainer.setPrefetchCount(prefetch);
        }
        if (props.isConsumerBatchEnabled()) {
            // 本上下文实现 ChannelAwareBatchMessageListener，setMessageListener 时容器已自动识别为批量监听；
            // 此处只需开启消费端批量并设定批量大小（批量受容器 receiveTimeout/batchReceiveTimeout 兼顶）
            listenerContainer.setConsumerBatchEnabled(true);
            listenerContainer.setBatchSize(Math.max(1, props.getBatchSize()));
            listenerContainer.setBatchReceiveTimeout(props.getBatchReceiveTimeout());
        }
        return listenerContainer;
    }
}
