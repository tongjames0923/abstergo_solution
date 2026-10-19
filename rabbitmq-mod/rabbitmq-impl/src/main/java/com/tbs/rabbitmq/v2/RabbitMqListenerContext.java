package com.tbs.rabbitmq.v2;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * RabbitMQ 消费上下文（v2 记录驱动模型 · {@code @RabbitListener} 注解接入形态）
 * <p>
 * 与 {@link RabbitMqContext}（自装配 {@code SimpleMessageListenerContainer}）二选一：本类<b>不建容器</b>，
 * 依托 Spring AMQP 的 {@code @RabbitListener} 基础设施。厂商无关的公共部分（传输层固定 {@link Message}、
 * 逐条 {@code basicAck}/{@code basicNack}、{@link ThreadLocal} 通道承载）复用 {@link AbstractRabbitMqContext}。
 * </p>
 * <p>
 * <b>子类职责</b>：声明 {@code @RabbitListener} 方法，在方法体里把消息灌入本类的接收入口即可。
 * 接收入口内部会 {@code set} 当前通道后调用 {@code messageReceived}，使建档与逐条确认都在投递线程内同步完成。
 * </p>
 * <p>
 * 单条接入：
 * <pre>{@code
 * @RabbitListener(queues = "order.queue", containerFactory = "manualAckContainerFactory")
 * public void listen(Message message, Channel channel) {
 *     handleMessage(message, channel);
 * }
 * }</pre>
 * 批量接入（工厂需 {@code setBatchListener(true)}）：
 * <pre>{@code
 * @RabbitListener(queues = "order.queue", containerFactory = "manualAckBatchContainerFactory")
 * public void listenBatch(List<Message> messages, Channel channel) {
 *     handleMessageBatch(messages, channel);
 * }
 * }</pre>
 * </p>
 * <p>
 * <b>关键前提</b>：所用 {@code RabbitListenerContainerFactory} 必须配置
 * {@code AcknowledgeMode.MANUAL}——本模型以记录为准，确认时机由框架在建档成功/失败时精确触发；
 * AUTO/AUTO_ACK 会让容器抢先确认、绕过该语义。方法参数里的 {@link Channel} 与消息自带的
 * {@code deliveryTag} 共同构成逐条确认的依据。
 * </p>
 *
 * @param <T> 业务负载类型（消费者面对的类型）
 * @author tongj
 * @since 2.0.0
 */
@Slf4j
public abstract class RabbitMqListenerContext<T extends Serializable> extends AbstractRabbitMqContext<T> {

    /**
     * 单条投递接收入口：供 {@code @RabbitListener(Message, Channel)} 方法体调用
     *
     * @param message 厂商原始消息
     * @param channel 投递通道，用于逐条确认
     */
    public final void handleMessage(Message message, Channel channel) {
        log.debug("[MQ-v2] @RabbitListener single handle, contextId={}, deliveryTag={}", getContextId(),
            message == null ? null : message.getMessageProperties().getDeliveryTag());
        ingest(Collections.singletonList(message), channel);
    }

    /**
     * 批量投递接收入口：供 {@code @RabbitListener(List<Message>, Channel)} 方法体调用
     * <p>
     * 批量仅把多条消息一次性喂进接收段做批量建档；确认仍逐条 {@code basicAck(tag,false)}
     * （每条自带 deliveryTag，同线程同通道安全）。
     * </p>
     *
     * @param messages 厂商原始消息批次，空批次直接忽略
     * @param channel  投递通道，批内消息均来自该通道
     */
    public final void handleMessageBatch(List<Message> messages, Channel channel) {
        log.debug("[MQ-v2] @RabbitListener batch handle, contextId={}, batchSize={}", getContextId(),
            messages == null ? 0 : messages.size());
        ingest(messages, channel);
    }
}
