package com.tbs.mq.v2.core;

/**
 * 消息体转换器（v2 记录驱动模型 · 厂商无关序列化契约）
 * <p>
 * 定义业务负载（T）与传输字节（byte[]）之间的双向转换，使 v2 收发链路摆脱厂商消息转换器（如 Spring AMQP
 * {@code MessageConverter}），改由框架自持、按格式可插拔：生产端用 {@code convert(T, Class)} 序列化 payload，
 * 消费端用 {@code convert(byte[], Class)} 还原，两端通过消息头 {@code payloadFormat} 选定同一实现、
 * 通过 {@code payloadType} 提供反序列化目标类，从而严格对称。
 * 一个实现对应一种 {@link #payloadFormat()}，多实现经注册表按格式路由。
 * </p>
 *
 * @author tongj
 * @since 2.0.0
 */
public interface IMqMessagePayloadConverter {

    /**
     * 消息体格式标识
     * <p>
     * 与消息头 {@code payloadFormat} 取值一致，作为注册表按格式选择转换器的路由键（如 {@code jdk}）。
     * </p>
     *
     * @return 格式标识，不为空
     */
    String payloadFormat();

    /**
     * 将业务负载序列化为字节
     *
     * @param payload 业务负载对象
     * @param clazz   负载类型
     * @param <T>     负载类型
     * @return 序列化后的字节数组，不为空
     */
    <T> byte[] convert(T payload, Class<T> clazz);

    /**
     * 将字节反序列化为业务负载
     *
     * @param payload 序列化字节
     * @param clazz   目标类型
     * @param <T>     目标类型
     * @return 反序列化后的业务负载对象
     */
    <T> T convert(byte[] payload, Class<T> clazz);
}
