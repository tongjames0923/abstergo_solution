package com.tbs.mq.v2.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.i18n.I18nUtil;
import com.tbs.mq.v2.core.IMqMessagePayloadConverter;
import com.tbs.mq.v2.core.MqPayloadConverterRegistry;

import java.io.*;

/**
 * JDK 序列化消息体转换器（v2 记录驱动模型 · 默认负载转换实现）
 * <p>
 * 以 JDK 原生序列化在负载（{@link java.io.Serializable}）与字节之间双向转换，格式标识为
 * {@link MqPayloadConverterRegistry#DEFAULT_PAYLOAD_FORMAT}（{@code jdk}）。零外部依赖、与传输层
 * {@code SerializerMessageConverter} 同源语义，作为单机/开发开箱即用的默认转换器；
 * 生产可另注册 JSON 等格式实现由 {@link MqPayloadConverterRegistry} 按格式路由，两端格式须一致。
 * 序列化失败按系统错误抛出并携带原因，供生产端上溯；反序列化失败交由接收段捕获后 nak。
 * </p>
 *
 * @author tongj
 * @since 2.0.0
 */
public class JdkMqPayloadConverter implements IMqMessagePayloadConverter {

    /**
     * JDK 序列化产物的 MIME 内容类型，与 Spring AMQP {@code SerializerMessageConverter} 写读的值一致，
     * 保证不带 payloadType 头的旧链路（厂商转换器）也能识别并反序列化
     */
    private static final String JAVA_SERIALIZED_CONTENT_TYPE = "application/x-java-serialized-object";

    @Override
    public String payloadFormat() {
        return MqPayloadConverterRegistry.DEFAULT_PAYLOAD_FORMAT;
    }

    @Override
    public String contentType() {
        return JAVA_SERIALIZED_CONTENT_TYPE;
    }

    /**
     * 将业务负载 JDK 序列化为字节
     *
     * @param payload 业务负载对象
     * @param clazz   负载类型
     * @param <T>     负载类型
     * @return 序列化后的字节数组
     */
    @Override
    public <T> byte[] convert(T payload, Class<T> clazz) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(payload);
            oos.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            FormattedRuntimeException error = new FormattedRuntimeException(e,
                I18nUtil.get("mq.v2.payload.serialize.failed", clazz == null ? null : clazz.getName()));
            AssertUtil.throwError(() -> error);
            throw error;
        }
    }

    /**
     * 将字节 JDK 反序列化为业务负载
     *
     * @param payload 序列化字节
     * @param clazz   目标类型
     * @param <T>     目标类型
     * @return 反序列化后的业务负载对象
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T convert(byte[] payload, Class<T> clazz) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(payload); ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (T)ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            FormattedRuntimeException error = new FormattedRuntimeException(e,
                I18nUtil.get("mq.v2.payload.deserialize.failed", clazz == null ? null : clazz.getName()));
            AssertUtil.throwError(() -> error);
            throw error;
        }
    }
}
