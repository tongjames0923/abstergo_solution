package com.tbs.mq.v2.impl;

import com.tbs.mq.v2.consume.IConsumerRegistry;
import com.tbs.mq.v2.consume.IMqConsumer;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 消费者注册表的内存基本实现（v2 记录驱动模型）
 * <p>
 * 以业务负载类型（T 的 Class）为路由键，维护"负载类型 → 消费者集合"的进程内映射：
 * {@link #register} / {@link #unregister} 支持运行期动态增删，用 {@link CopyOnWriteArrayList}
 * 承载同一类型下的多消费者，保证分发线程遍历与注册线程变更互不干扰（弱一致迭代，无并发修改异常）。
 * 未注册的负载类型返回空集合，分发段据此判定"无消费者"并拒收，属预期保护。
 * </p>
 * <p>
 * 定位为单机/开发/自检用的开箱即用实现，不含任何厂商或外部存储依赖；集群环境应替换为分布式注册实现。
 * </p>
 *
 * @author tongj
 * @since 2.0.0
 */
public class InMemoryConsumerRegistry implements IConsumerRegistry {

    /**
     * 负载类型到消费者集合的路由表，键为消费者注册时声明的负载 Class
     */
    private final Map<Class<?>, List<IMqConsumer<?>>> registry = new ConcurrentHashMap<>();

    /**
     * 注册消费者到其负载类型的路由桶
     *
     * @param consumer    待注册的消费者，为空则忽略
     * @param messageType 业务负载类型，路由键，为空则忽略
     * @param <T>         业务负载类型
     */
    @Override
    public <T extends Serializable> void register(IMqConsumer<T> consumer, Class<T> messageType) {
        if (consumer == null || messageType == null) {
            return;
        }
        registry.computeIfAbsent(messageType, k -> new CopyOnWriteArrayList<>()).add(consumer);
    }

    /**
     * 从负载类型的路由桶注销消费者，桶空则回收路由键
     *
     * @param messageType 业务负载类型，路由键，为空则忽略
     * @param consumer    待注销的消费者，为空则忽略
     * @param <T>         业务负载类型
     */
    @Override
    public <T extends Serializable> void unregister(Class<T> messageType, IMqConsumer<T> consumer) {
        if (messageType == null || consumer == null) {
            return;
        }
        List<IMqConsumer<?>> consumers = registry.get(messageType);
        if (consumers != null) {
            consumers.remove(consumer);
            if (consumers.isEmpty()) {
                registry.remove(messageType, consumers);
            }
        }
    }

    /**
     * 按负载类型回查全部已注册消费者
     *
     * @param payloadType 业务负载类型，路由键
     * @param <T>         业务负载类型
     * @return 该负载类型的消费者集合（只读视图），未注册时返回空集合
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Serializable> List<IMqConsumer<T>> getConsumers(Class<T> payloadType) {
        List<IMqConsumer<?>> consumers = registry.get(payloadType);
        if (consumers == null) {
            return Collections.emptyList();
        }
        return (List<IMqConsumer<T>>) (List<?>) Collections.unmodifiableList(consumers);
    }
}
