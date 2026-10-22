package com.tbs.rabbitmq.v2;

import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.i18n.I18nUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.mq.constants.MqHeaderConstants;
import com.tbs.mq.v2.core.AbstractMqMessageReceiver;
import com.tbs.mq.v2.core.IMqMessagePayloadConverter;
import com.tbs.mq.v2.core.IMqRecorder;
import com.tbs.mq.v2.core.MqPayloadConverterRegistry;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.support.converter.MessageConverter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ 消息接收器（v2 记录驱动模型 · 接收段的 RabbitMQ 落地）
 * <p>
 * 把厂商传输层固定为 Spring AMQP 的 {@link Message}（R），只补齐 RabbitMQ 特有的三个接收钩子：
 * <ul>
 *     <li>{@link #getPayload} —— 对称于 v2 生产端：消息头携带 {@code payloadType} 时走 {@link MqPayloadConverterRegistry}
 *         按 {@code payloadFormat} 选定的 {@link IMqMessagePayloadConverter} 将字节还原为业务负载（T）；
 *         无 {@code payloadType} 头（如 v1 生产端经厂商 {@link MessageConverter} 投递）时沿用厂商转换器兜底；</li>
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
     * 业务标识消息头键，生产者需在消息头写入该字段作为幂等去重与人工排查的锚点；
     * 与生产端写入的 {@link MqHeaderConstants#BUSINESS_ID} 保持单一真源
     */
    public static final String BUSINESS_ID_HEADER = MqHeaderConstants.BUSINESS_ID;

    @Resource
    private MessageConverter messageConverter;

    /**
     * 负载转换器注册表，当消息头携带 payloadType 时按 payloadFormat 选取转换器反序列化
     */
    @Resource
    private MqPayloadConverterRegistry mqPayloadConverterRegistry;

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
     * <p>
     * 消息头携带 {@code payloadType} 时走 v2 原生负载转换器（与生产端对称）：按 {@code payloadFormat}
     * 选定转换器，以 {@code payloadType} 为目标类将消息体字节还原；无 {@code payloadType} 头时沿用厂商
     * {@link MessageConverter} 兜底（兼容 v1 生产端直投）。
     * </p>
     *
     * @param message 厂商原始消息
     * @return 业务负载对象
     */
    @Override
    @SuppressWarnings("unchecked")
    protected T getPayload(Message message) {
        Object payloadTypeHeader = message.getMessageProperties().getHeaders().get(MqHeaderConstants.PAYLOAD_TYPE);
        if (payloadTypeHeader == null) {
            T legacy = (T)messageConverter.fromMessage(message);
            log.debug("[MQ-v2] deserialize via vendor converter (no payloadType header), deliveryTag={}, payloadType={}",
                message.getMessageProperties().getDeliveryTag(), legacy == null ? null : legacy.getClass().getName());
            return legacy;
        }
        String payloadType = String.valueOf(payloadTypeHeader);
        Object formatHeader = message.getMessageProperties().getHeaders().get(MqHeaderConstants.PAYLOAD_FORMAT);
        String payloadFormat = formatHeader == null ? null : String.valueOf(formatHeader);
        IMqMessagePayloadConverter converter = mqPayloadConverterRegistry.resolve(payloadFormat);
        try {
            T payload = (T)converter.convert(message.getBody(), Class.forName(payloadType));
            log.debug("[MQ-v2] deserialize via payload converter, format={}, payloadType={}", payloadFormat, payloadType);
            return payload;
        } catch (ClassNotFoundException e) {
            throw new FormattedRuntimeException(e, I18nUtil.get("mq.v2.payload.deserialize.failed", payloadType));
        }
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
        if (StrUtil.isNotEmpty(brokerMessageId)) {
            log.debug("[MQ-v2] genMessageId reuse broker messageId={}", brokerMessageId);
            return brokerMessageId;
        }
        String fallback = String.valueOf(message.getMessageProperties().getDeliveryTag());
        log.debug("[MQ-v2] genMessageId broker messageId absent, fallback to deliveryTag={}", fallback);
        return fallback;
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
            log.debug("[MQ-v2] resolveBusinessId from header={}", businessId);
            return String.valueOf(businessId);
        }
        String consumerQueue = message.getMessageProperties().getConsumerQueue();
        log.debug("[MQ-v2] businessId header absent, fallback to consumerQueue={}", consumerQueue);
        return consumerQueue;
    }

    /**
     * 提取消息头
     * <p>
     * 把 AMQP 头的任意类型值统一转为字符串，供消费链路按 {@link MqHeaderConstants} 中的键读取
     * （如 {@link MqHeaderConstants#OPERATOR_ID} 操作人）；空值不保留。
     * </p>
     *
     * @param message 厂商原始消息
     * @return 消息头键值
     */
    @Override
    protected Map<String, String> resolveHeaders(Message message) {
        Map<String, Object> rawHeaders = message.getMessageProperties().getHeaders();
        Map<String, String> headers = new HashMap<>(rawHeaders.size());
        rawHeaders.forEach((key, value) -> {
            if (value != null) {
                headers.put(key, String.valueOf(value));
            }
        });
        return headers;
    }
}
