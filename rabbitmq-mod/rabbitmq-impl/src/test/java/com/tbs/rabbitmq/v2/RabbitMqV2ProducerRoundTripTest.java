package com.tbs.rabbitmq.v2;

import com.tbs.mq.constants.MqHeaderConstants;
import com.tbs.mq.v2.core.IMqRecorder;
import com.tbs.mq.v2.core.MqPayloadConverterRegistry;
import com.tbs.mq.v2.impl.JdkMqPayloadConverter;
import com.tbs.mq.v2.vo.MqMessageOutcomeVO;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.SerializerMessageConverter;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RabbitMQ v2 生产/接收闭环自检
 * <p>
 * 无 broker 验证 v2 原生收发对称：{@link RabbitMqMessageProducer#toVendorMessage} 组装的信封经
 * {@link RabbitMqMessageReceiver#getPayload} 能还原回原负载，且 messageId/payloadType/payloadFormat/businessId
 * 落到约定的消息属性与头；同时验证无 {@code payloadType} 头时接收段回退到厂商转换器（兼容 v1 生产端直投）。
 * </p>
 *
 * @author abstergo
 * @since 2.0.0
 */
class RabbitMqV2ProducerRoundTripTest {

    /**
     * 反射设置私有字段（含父类字段），避免引入 Spring/Mockito
     */
    private static void setField(Object target, String name, Object value) throws Exception {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    /**
     * 具体接收器薄壳：recorder 传 null（getPayload 不触碰记录层），仅提供负载类型
     */
    private static final class ProbeReceiver extends RabbitMqMessageReceiver<String> {
        ProbeReceiver(IMqRecorder<Message, String> recorder) {
            super(recorder);
        }
    }

    /**
     * 仅注册默认 jdk 转换器的注册表
     */
    private static MqPayloadConverterRegistry jdkRegistry() {
        return new MqPayloadConverterRegistry(Collections.singletonList(new JdkMqPayloadConverter()));
    }

    @Test
    void toVendorMessageShouldCarryIdAndHeaders() {
        RabbitMqMessageProducer producer = new RabbitMqMessageProducer(null, jdkRegistry());
        MqMessageOutcomeVO<String> envelope = MqMessageOutcomeVO.<String>builder().payload("hello-v2").topic("v2.exchange")
            .routeKey("v2.rk").businessId("biz-1").build();

        Message message = producer.toVendorMessage(envelope);
        MessageProperties props = message.getMessageProperties();

        assertNotNull(props.getMessageId(), "messageId 为空时应框架发号");
        assertEquals("application/x-java-serialized-object", props.getContentType(),
            "jdk 转换器声明的 contentType 应写入，供厂商转换器（v1 链路）兜底识别");
        assertEquals("java.lang.String", props.getHeaders().get(MqHeaderConstants.PAYLOAD_TYPE));
        assertEquals(MqPayloadConverterRegistry.DEFAULT_PAYLOAD_FORMAT, props.getHeaders().get(MqHeaderConstants.PAYLOAD_FORMAT));
        assertEquals("biz-1", props.getHeaders().get(MqHeaderConstants.BUSINESS_ID));
    }

    @Test
    void producerAndReceiverShouldRoundTripPayload() throws Exception {
        MqPayloadConverterRegistry registry = jdkRegistry();
        RabbitMqMessageProducer producer = new RabbitMqMessageProducer(null, registry);
        ProbeReceiver receiver = new ProbeReceiver(null);
        setField(receiver, "mqPayloadConverterRegistry", registry);
        setField(receiver, "messageConverter", new SerializerMessageConverter());

        MqMessageOutcomeVO<String> envelope = MqMessageOutcomeVO.<String>builder().messageId("m-100")
            .payload("round-trip-payload").topic("v2.exchange").routeKey("v2.rk").build();

        Message message = producer.toVendorMessage(envelope);
        assertEquals("m-100", message.getMessageProperties().getMessageId(), "显式 messageId 应原样保留");

        String decoded = receiver.getPayload(message);
        assertEquals("round-trip-payload", decoded, "生产端字节应被接收端经同一转换器还原");
        message.getMessageProperties().setConsumerQueue("v2.queue");
        assertEquals("v2.queue", receiver.resolveBusinessId(message), "businessId 头缺失时应回落消费者队列名");
    }

    @Test
    void resolveBusinessIdShouldReadHeader() throws Exception {
        ProbeReceiver receiver = new ProbeReceiver(null);
        setField(receiver, "mqPayloadConverterRegistry", jdkRegistry());
        setField(receiver, "messageConverter", new SerializerMessageConverter());
        RabbitMqMessageProducer producer = new RabbitMqMessageProducer(null, jdkRegistry());

        MqMessageOutcomeVO<String> envelope = MqMessageOutcomeVO.<String>builder().payload("x").businessId("order-9")
            .topic("v2.exchange").routeKey("v2.rk").build();
        Message message = producer.toVendorMessage(envelope);

        assertEquals("order-9", receiver.resolveBusinessId(message), "businessId 应优先取消息头");
    }

    @Test
    void getPayloadShouldFallbackToVendorConverterWhenNoPayloadTypeHeader() throws Exception {
        SerializerMessageConverter vendorConverter = new SerializerMessageConverter();
        ProbeReceiver receiver = new ProbeReceiver(null);
        setField(receiver, "mqPayloadConverterRegistry", jdkRegistry());
        setField(receiver, "messageConverter", vendorConverter);

        MessageProperties props = new MessageProperties();
        props.setMessageId("legacy-1");
        Message legacy = vendorConverter.toMessage("legacy-payload", props);
        assertNull(legacy.getMessageProperties().getHeaders().get(MqHeaderConstants.PAYLOAD_TYPE), "厂商消息不应带 payloadType 头");

        String decoded = receiver.getPayload(legacy);
        assertEquals("legacy-payload", decoded, "无 payloadType 头应回退厂商转换器");
    }

    @Test
    void vendorConverterShouldReadV2MessageForLegacyConsumers() {
        RabbitMqMessageProducer producer = new RabbitMqMessageProducer(null, jdkRegistry());
        MqMessageOutcomeVO<String> envelope = MqMessageOutcomeVO.<String>builder().payload("for-v1").topic("v2.exchange")
            .routeKey("v2.rk").build();

        Message message = producer.toVendorMessage(envelope);
        Object decoded = new SerializerMessageConverter().fromMessage(message);
        assertEquals("for-v1", decoded, "v2 生产消息应可被 v1 厂商转换器凭 contentType 反序列化");
    }

    @Test
    void registryShouldRejectUnknownFormat() {
        MqPayloadConverterRegistry registry = jdkRegistry();
        assertThrows(RuntimeException.class, () -> registry.resolve("protobuf"), "未知格式应断言拒绝");
        assertEquals(MqPayloadConverterRegistry.DEFAULT_PAYLOAD_FORMAT,
            assertDoesNotThrow(() -> registry.resolve(null).payloadFormat()), "空格式应回落默认转换器");
    }
}
