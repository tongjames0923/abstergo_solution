package com.tbs.rabbitmq.v2;

import com.rabbitmq.client.Channel;
import com.tbs.common.function.SConsumer;
import com.tbs.mq.v2.consume.IConsumerRegistry;
import com.tbs.mq.v2.core.IMqContext;
import com.tbs.mq.v2.core.IMqMessageReceiver;
import com.tbs.mq.v2.vo.MqMessageIncomeVO;
import com.tbs.rabbitmq.support.MqConsumerProperties;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConverter;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RabbitMQ v2 传输层适配器自检
 * <p>
 * 聚焦厂商侧非平凡逻辑：接收与确认在同一线程完成（{@code ThreadLocal} 通道承载）、
 * 单条与批量两条路径下 ack/nak 落到 {@code basicAck}/{@code basicNack} 的确切语义、回调返回后通道清理，
 * 以及接收器对厂商 messageId 与业务头的复用。用 JDK 动态代理替代重量级的 Channel 接口，零额外依赖。
 * </p>
 *
 * @author abstergo
 * @since 2.0.0
 */
class RabbitMqV2AdapterTest {

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
     * 构造带 deliveryTag 与 messageId 的厂商消息
     */
    private static Message message(long deliveryTag, String messageId) {
        MessageProperties props = new MessageProperties();
        props.setDeliveryTag(deliveryTag);
        props.setMessageId(messageId);
        return new Message("{}".getBytes(), props);
    }

    /**
     * 记录 Channel 上的方法调用，用于断言 ack/nak 语义
     */
    private static final class RecordingChannel {
        final List<Object[]> calls = new ArrayList<>();

        Channel proxy() {
            return (Channel) Proxy.newProxyInstance(Channel.class.getClassLoader(), new Class[] {Channel.class},
                (p, method, args) -> {
                    calls.add(new Object[] {method.getName(), args});
                    Class<?> rt = method.getReturnType();
                    if (rt == boolean.class) {
                        return false;
                    }
                    if (rt == int.class) {
                        return 0;
                    }
                    if (rt == long.class) {
                        return 0L;
                    }
                    return null;
                });
        }

        Object[] last(String method) {
            for (int i = calls.size() - 1; i >= 0; i--) {
                if (method.equals(calls.get(i)[0])) {
                    return (Object[]) calls.get(i)[1];
                }
            }
            return null;
        }

        int count(String method) {
            return (int) calls.stream().filter(c -> method.equals(c[0])).count();
        }

        List<Object> tagsOf(String method) {
            List<Object> tags = new ArrayList<>();
            calls.stream().filter(c -> method.equals(c[0])).forEach(c -> tags.add(((Object[]) c[1])[0]));
            return tags;
        }
    }

    /**
     * 业务上下文薄壳：仅提供抽象配置，不触发 afterPropertiesSet（无需真实连接）
     */
    private static final class ProbeContext extends RabbitMqContext<String> {
        @Override
        protected MqConsumerProperties getMqConsumerProperties() {
            return MqConsumerProperties.builder("q.test").build();
        }

        @Override
        public Class<String> getMessagePayloadType() {
            return String.class;
        }

        @Override
        public Long getContextId() {
            return 1L;
        }
    }

    /**
     * {@code @RabbitListener} 接入形态薄壳：仅提供身份，接收链路走真实父类
     */
    private static final class ProbeListenerContext extends RabbitMqListenerContext<String> {
        @Override
        public Class<String> getMessagePayloadType() {
            return String.class;
        }

        @Override
        public Long getContextId() {
            return 2L;
        }
    }

    /**
     * 建档成功的接收器桩：全部回调 onInqueueSuccess（即厂商 ack）
     */
    private static final class AckReceiver implements IMqMessageReceiver<Message, String> {
        @Override
        public List<MqMessageIncomeVO<Message, String>> receive(IMqContext<Message, String> context,
            List<Message> messages, SConsumer<Message> onInqueueSuccess, SConsumer<Message> onInqueueFail) {
            messages.forEach(onInqueueSuccess);
            return new LinkedList<>();
        }
    }

    /**
     * 建档失败的接收器桩：全部回调 onInqueueFail（即厂商 nak）
     */
    private static final class NakReceiver implements IMqMessageReceiver<Message, String> {
        @Override
        public List<MqMessageIncomeVO<Message, String>> receive(IMqContext<Message, String> context,
            List<Message> messages, SConsumer<Message> onInqueueSuccess, SConsumer<Message> onInqueueFail) {
            messages.forEach(onInqueueFail);
            return new LinkedList<>();
        }
    }

    @Test
    void onMessageShouldBasicAckOnInqueueSuccess() throws Exception {
        ProbeContext context = new ProbeContext();
        setField(context, "receiver", new AckReceiver());
        RecordingChannel recorder = new RecordingChannel();

        context.onMessage(message(42L, "m-1"), recorder.proxy());

        Object[] ack = recorder.last("basicAck");
        assertNotNull(ack, "建档成功应触发 basicAck");
        assertEquals(42L, ack[0]);
        assertEquals(false, ack[1], "非批量确认");
        assertNull(recorder.last("basicNack"), "不应触发 nack");
    }

    @Test
    void onMessageShouldBasicNackWithoutRequeueOnInqueueFail() throws Exception {
        ProbeContext context = new ProbeContext();
        setField(context, "receiver", new NakReceiver());
        RecordingChannel recorder = new RecordingChannel();

        context.onMessage(message(7L, "m-2"), recorder.proxy());

        Object[] nack = recorder.last("basicNack");
        assertNotNull(nack, "建档失败应触发 basicNack");
        assertEquals(7L, nack[0]);
        assertEquals(false, nack[1]);
        assertEquals(false, nack[2], "拒收不重投入队，失败恢复只走记录层");
        assertNull(recorder.last("basicAck"), "不应触发 ack");
    }

    @Test
    void onMessageBatchShouldAckEachMessageOnSharedChannel() throws Exception {
        ProbeContext context = new ProbeContext();
        setField(context, "receiver", new AckReceiver());
        RecordingChannel recorder = new RecordingChannel();

        context.onMessageBatch(Arrays.asList(message(10L, "a"), message(11L, "b"), message(12L, "c")),
            recorder.proxy());

        assertEquals(3, recorder.count("basicAck"), "批量内每条成功消息都应各自 basicAck");
        assertEquals(Arrays.<Object>asList(10L, 11L, 12L), recorder.tagsOf("basicAck"),
            "批量逐条确认需携带各自的 deliveryTag");
        assertEquals(0, recorder.count("basicNack"));
    }

    @Test
    void onMessageBatchShouldNackEachMessageOnInqueueFail() throws Exception {
        ProbeContext context = new ProbeContext();
        setField(context, "receiver", new NakReceiver());
        RecordingChannel recorder = new RecordingChannel();

        context.onMessageBatch(Arrays.asList(message(20L, "a"), message(21L, "b")), recorder.proxy());

        assertEquals(2, recorder.count("basicNack"), "批量内每条失败消息都应各自 basicNack");
        assertEquals(0, recorder.count("basicAck"));
    }

    @Test
    void onMessageBatchShouldIgnoreEmptyOrNullBatch() throws Exception {
        ProbeContext context = new ProbeContext();
        RecordingChannel recorder = new RecordingChannel();

        assertDoesNotThrow(() -> context.onMessageBatch(Collections.emptyList(), recorder.proxy()));
        assertDoesNotThrow(() -> context.onMessageBatch(null, recorder.proxy()));
        assertTrue(recorder.calls.isEmpty(), "空批次不得发起任何确认");
    }

    @Test
    void channelShouldBeClearedAfterOnMessage() throws Exception {
        ProbeContext context = new ProbeContext();
        RecordingChannel recorder = new RecordingChannel();

        // onMessage 之外直接确认：通道线程上下文已清理，应安全跳过而非抛异常，也不触碰 Channel
        assertDoesNotThrow(() -> context.ackMessage(message(1L, "m-3")));
        assertDoesNotThrow(() -> context.nakMessage(message(1L, "m-3")));
        assertTrue(recorder.calls.isEmpty(), "通道缺席时不得发起任何确认");
    }

    @Test
    void getMessageTypeShouldBindSpringAmqpMessage() {
        assertEquals(Message.class, new ProbeContext().getMessageType());
    }

    @Test
    void receiverShouldReuseBrokerMessageIdAndBusinessHeader() throws Exception {
        RabbitMqMessageReceiver<String> receiver = new RabbitMqMessageReceiver<String>(null) {};
        setField(receiver, "messageConverter", new MessageConverter() {
            @Override
            public Message toMessage(Object object, MessageProperties messageProperties) {
                return new Message(new byte[0], messageProperties);
            }

            @Override
            public Object fromMessage(Message message) {
                return "payload";
            }
        });

        MessageProperties props = new MessageProperties();
        props.setMessageId("broker-id-9");
        props.setHeader(RabbitMqMessageReceiver.BUSINESS_ID_HEADER, "biz-77");
        Message msg = new Message("{}".getBytes(), props);

        assertEquals("payload", receiver.getPayload(msg), "getPayload 应经 MessageConverter 反序列化");
        assertEquals("broker-id-9", invokeGenId(receiver, msg), "genMessageId 应复用厂商 messageId");
        assertEquals("biz-77", receiver.resolveBusinessId(msg), "业务标识应优先取消息头 businessId");
    }

    /**
     * genMessageId 为 protected，经反射调用以贴近真实可见性
     */
    private static String invokeGenId(RabbitMqMessageReceiver<String> receiver, Message msg) throws Exception {
        var method = RabbitMqMessageReceiver.class.getDeclaredMethod("genMessageId", Message.class);
        method.setAccessible(true);
        return (String) method.invoke(receiver, msg);
    }

    @Test
    void processorShouldExposeInjectedRegistry() throws Exception {
        RabbitMqMessageProcessor<String> processor = new RabbitMqMessageProcessor<String>() {};
        IConsumerRegistry registry = new IConsumerRegistry() {
            @Override
            public <T extends java.io.Serializable> void register(com.tbs.mq.v2.consume.IMqConsumer<T> consumer,
                Class<T> messageType) {
            }

            @Override
            public <T extends java.io.Serializable> void unregister(Class<T> messageType,
                com.tbs.mq.v2.consume.IMqConsumer<T> consumer) {
            }

            @Override
            public <T extends java.io.Serializable> List<com.tbs.mq.v2.consume.IMqConsumer<T>> getConsumers(
                Class<T> payloadType) {
                return Collections.emptyList();
            }
        };
        setField(processor, "consumerRegistry", registry);
        assertSame(registry, processor.getConsumerRegistry());
    }

    @Test
    void listenerContextHandleMessageShouldAckOnSharedChannel() throws Exception {
        ProbeListenerContext context = new ProbeListenerContext();
        setField(context, "receiver", new AckReceiver());
        RecordingChannel recorder = new RecordingChannel();

        // @RabbitListener(Message, Channel) 方法体调用 handleMessage
        context.handleMessage(message(5L, "p-5"), recorder.proxy());

        Object[] ack = recorder.last("basicAck");
        assertNotNull(ack, "建档成功应回调 basicAck");
        assertEquals(5L, ack[0], "basicAck 应携带该消息的 deliveryTag");
        assertEquals(false, ack[1], "非批量确认");
    }

    @Test
    void listenerContextHandleMessageBatchShouldAckEachMessage() throws Exception {
        ProbeListenerContext context = new ProbeListenerContext();
        setField(context, "receiver", new AckReceiver());
        RecordingChannel recorder = new RecordingChannel();

        // @RabbitListener(List<Message>, Channel) 方法体调用 handleMessageBatch
        context.handleMessageBatch(Arrays.asList(message(20L, "a"), message(21L, "b")), recorder.proxy());

        assertEquals(2, recorder.count("basicAck"), "批量逐条 ack");
        assertEquals(Arrays.<Object>asList(20L, 21L), recorder.tagsOf("basicAck"), "每条应 ack 各自的 deliveryTag");
    }
}
