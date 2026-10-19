package com.tbs.rabbitmq.v2;

import com.tbs.common.util.base.StrUtil;
import com.tbs.mq.v2.core.AbstractMqMessageReceiver;
import com.tbs.mq.v2.core.IMqRecorder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.support.converter.MessageConverter;

import java.io.Serializable;

/**
 * RabbitMQ 消息接收器（v2 记录驱动模型 · 接收段的 RabbitMQ 落地）
 * <p>
 * 把厂商传输层固定为 Spring AMQP 的 {@link Message}（R），只补齐 RabbitMQ 特有的三个接收钩子：
 * <ul>
 *     <li>{@link #getPayload} —— 经容器内的 {@link MessageConverter} 把消息体反序列化为业务负载（T）；</li>
 *     <li>{@link #genMessageId} —— 优先复用厂商投递携带的 messageId 作为记录主键（跨节点同 ID 天然去重），缺失时回落框架 UUID；</li>
 *     <li>{@link #resolveBusinessId} —— 默认按消息头 {@code businessId} 取值，其次回落厂商 messageId，均无时返回空由基类拒收。</li>
 * </ul>
 * 组装/校验/建档/配对/确认的编排均由父类 {@link AbstractMqMessageReceiver} 承担，本类不涉及。
 * 抽象类，业务子类绑定具体负载类型 T 并声明为 Spring Bean 即可，recorder 由构造器传入。
 * </p>
 *
 * @param <T> 业务负载类型（消费者面对的类型）
 * @author tongj
 * @since 2.0.0
 */
@Slf4j
public abstract class RabbitMqMessageReceiver<T extends Serializable> extends AbstractMqMessageReceiver<Message, T> {

    /**
     * 业务标识消息头键，生产者需在消息头写入该字段作为幂等去重与人工排查的锚点
     */
    public static final String BUSINESS_ID_HEADER = "businessId";

    @Resource
    private MessageConverter messageConverter;

    /**
     * 构造 RabbitMQ 消息接收器
     *
     * @param recorder 消息记录器，接收段建档的执行方
     */
    public RabbitMqMessageReceiver(IMqRecorder<Message, T> recorder) {
        super(recorder);
    }

    /**
     * 反序列化业务负载
     *
     * @param message 厂商原始消息
     * @return 业务负载对象
     */
    @Override
    @SuppressWarnings("unchecked")
    protected T getPayload(Message message) {
        return (T)messageConverter.fromMessage(message);
    }

    /**
     * 生成消息ID
     * <p>
     * 复用厂商 messageId 使同一投递在各节点得到同一记录主键，配合记录层实现天然去重；无则回落消息的 deliveryTag。
     * </p>
     *
     * @param message 厂商原始消息
     * @return 全局唯一的消息ID
     */
    @Override
    protected String genMessageId(Message message) {
        String brokerMessageId = message.getMessageProperties().getMessageId();
        return StrUtil.isNotEmpty(brokerMessageId) ? brokerMessageId
            : String.valueOf(message.getMessageProperties().getDeliveryTag());
    }

    /**
     * 解析业务标识
     * <p>
     * 默认从消息头 {@code businessId} 读取，缺失时回落消费者队列名。业务有更精确的锚点（如订单号）时覆盖本方法。
     * </p>
     *
     * @param message 厂商原始消息
     * @return 业务标识，返回空将由基类校验拒收
     */
    @Override
    protected String resolveBusinessId(Message message) {
        Object businessId = message.getMessageProperties().getHeaders().get(BUSINESS_ID_HEADER);
        if (businessId != null && StrUtil.isNotEmpty(String.valueOf(businessId))) {
            return String.valueOf(businessId);
        }
        log.warn("Business ID not found in message headers, falling back to consumer queue name.");
        return message.getMessageProperties().getConsumerQueue();
    }
}
