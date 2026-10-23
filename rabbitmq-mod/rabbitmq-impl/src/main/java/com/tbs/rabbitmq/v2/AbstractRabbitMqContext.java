package com.tbs.rabbitmq.v2;

import com.rabbitmq.client.Channel;
import com.tbs.common.util.base.CollUtil;
import com.tbs.mq.v2.core.AbstractMqContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;

import java.io.Serializable;
import java.util.List;

/**
 * RabbitMQ v2 上下文的厂商无关公共基类
 * <p>
 * 把与 RabbitMQ Broker 打交道的"易错且唯一"的一段收敛到此处，供两种接入形态的基类复用：
 * <ul>
 *     <li>传输层固定为 Spring AMQP 的 {@link Message}（{@link #getMessageType()}）；</li>
 *     <li>确认语义固定为逐条 {@code basicAck(tag,false)} / {@code basicNack(tag,false,false)}
 *     —— 不 requeue，建档失败的恢复只走记录层，不靠厂商重投；</li>
 *     <li>通道以 {@link ThreadLocal} 承载：建档与 ack/nak 都在投递线程内同步完成
 *     （接收段 {@code receive} 内联回调 {@code ackMessage}/{@code nakMessage}），处理线程不触碰厂商确认，
 *     故无需跨线程 {@code Message→Channel} 映射。</li>
 * </ul>
 * 子类只需把"消息怎么进来"对接好，调用 {@link #ingest(List, Channel)} 即可复用整条记录驱动链路。
 * </p>
 *
 * @param <T> 业务负载类型（消费者面对的类型）
 * @author tongj
 * @since 2.0.0
 */
@Slf4j
public abstract class AbstractRabbitMqContext<T extends Serializable> extends AbstractMqContext<Message, T> {

    /**
     * 当前投递线程绑定的通道，供同线程内的 ack/nak 取用
     */
    private final ThreadLocal<Channel> currentChannel = new ThreadLocal<>();

    /**
     * 统一接收入口：在投递线程内暂存通道，把整批原始消息交由框架建档编排
     * <p>
     * 批量仅将多条消息一次性喂进接收段做批量建档；确认仍是逐条 {@code basicAck(tag,false)} （每条自带 deliveryTag，同线程同通道安全）。
     * </p>
     *
     * @param messages 厂商原始消息批次，空批次直接忽略
     * @param channel  投递通道，批内消息均来自该通道
     */
    protected final void ingest(List<Message> messages, Channel channel) {
        if (CollUtil.isEmpty(messages)) {
            log.debug("[MQ-v2] ingest skip, empty batch, contextId={}", getContextId());
            return;
        }
        log.debug("[MQ-v2] ingest batch, contextId={}, size={}, channelOpen={}", getContextId(), messages.size(),
            channel != null && channel.isOpen());
        currentChannel.set(channel);
        try {
            messageReceived(messages);
        } finally {
            currentChannel.remove();
        }
    }

    /**
     * 结清厂商投递：对当前线程通道的该消息执行 basicAck
     *
     * @param message 建档成功的厂商原始消息
     */
    @Override
    public void ackMessage(Message message) {
        Channel channel = currentChannel.get();
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        if (channel == null) {
            log.error("[MQ-v2] ack failed: channel not bound to current thread, contextId={}, deliveryTag={}",
                getContextId(), deliveryTag);
            return;
        }
        try {
            channel.basicAck(deliveryTag, false);
            log.debug("[MQ-v2] basicAck success, contextId={}, deliveryTag={}", getContextId(), deliveryTag);
        } catch (Exception e) {
            log.error("[MQ-v2] basicAck error, contextId={}, deliveryTag={}", getContextId(), deliveryTag, e);
        }
    }

    /**
     * 拒收厂商投递：对当前线程通道的该消息执行 basicNack 且 notRequeue，失败恢复只走记录层
     *
     * @param message 建档失败的厂商原始消息
     */
    @Override
    public void nakMessage(Message message) {
        Channel channel = currentChannel.get();
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        if (channel == null) {
            log.error("[MQ-v2] nak failed: channel not bound to current thread, contextId={}, deliveryTag={}",
                getContextId(), deliveryTag);
            return;
        }
        try {
            channel.basicNack(deliveryTag, false, false);
            log.debug("[MQ-v2] basicNack success (no requeue), contextId={}, deliveryTag={}", getContextId(), deliveryTag);
        } catch (Exception e) {
            log.error("[MQ-v2] basicNack error, contextId={}, deliveryTag={}", getContextId(), deliveryTag, e);
        }
    }

    /**
     * 厂商原始消息类型固定为 Spring AMQP 的 {@link Message}
     *
     * @return {@link Message} 类型
     */
    @Override
    @SuppressWarnings("unchecked")
    public Class<Message> getMessageType() {
        return Message.class;
    }
}
