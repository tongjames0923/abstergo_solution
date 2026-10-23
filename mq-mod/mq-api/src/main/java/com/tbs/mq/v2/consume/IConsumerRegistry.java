package com.tbs.mq.v2.consume;

import java.io.Serializable;
import java.util.List;

/**
 * 消费者注册接口
 * <p>
 * 以业务负载类型（T）为键的消费者路由表：上下文分发时按 {@code getMessagePayloadType()} 回查消费者集合。
 * 注册与注销支持运行期动态变更：未注册任何消费者时上下文拒收/主记录 FAILED，属预期保护。
 * </p>
 */
public interface IConsumerRegistry {
    /**
     * 注册消费者
     *
     * @param consumer    待注册的消费者
     * @param messageType 业务负载类型，路由键
     * @param <T>         业务负载类型
     */
    <T extends Serializable> void register(IMqConsumer<T> consumer, Class<T> messageType);

    /**
     * 注销消费者
     *
     * @param messageType 业务负载类型，路由键
     * @param consumer    待注销的消费者
     * @param <T>         业务负载类型
     */
    <T extends Serializable> void unregister(Class<T> messageType, IMqConsumer<T> consumer);

    /**
     * 获取消费者
     *
     * @param payloadType 业务负载类型，路由键
     * @param <T>         业务负载类型
     * @return 该负载类型注册的全部消费者，未注册时返回空集合或 null（上下文按无消费者处理）
     */
    <T extends Serializable> List<IMqConsumer<T>> getConsumers(Class<T> payloadType);
}
