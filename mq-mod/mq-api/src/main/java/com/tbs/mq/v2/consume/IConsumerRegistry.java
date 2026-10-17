package com.tbs.mq.v2.consume;

import java.io.Serializable;
import java.util.List;

/**
 * 消费者注册接口
 */
public interface IConsumerRegistry {
    /**
     * 注册消费者
     *
     * @param consumer
     * @param messageType
     * @param context
     * @param <T>
     */
    <T extends Serializable> void register(IMqConsumer<T> consumer, Class<T> messageType);

    /**
     * 注销消费者
     *
     * @param messageType
     * @param consumer
     * @param <T>
     */
    <T extends Serializable> void unregister(Class<T> messageType, IMqConsumer<T> consumer);

    /**
     * 获取消费者
     *
     * @param messageType
     * @param <T>
     * @return
     */
    <T extends Serializable> List<IMqConsumer<T>> getConsumers(Class<T> payloadType);
}
