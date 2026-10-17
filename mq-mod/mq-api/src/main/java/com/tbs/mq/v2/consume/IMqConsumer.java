package com.tbs.mq.v2.consume;

import com.tbs.mq.v2.exception.MqConsumeException;

import java.io.Serializable;

/**
 * MQ消费者接口
 * <p>
 * 面向业务负载（T）的消费单元，通过 {@code IConsumerRegistry} 按负载类型注册；
 * 上下文分发时对每个注册消费者广播执行，各自结果记入消费台账，consumeId 是台账行的身份键组成部分。
 * </p>
 *
 * @param <T> 业务负载类型
 */
public interface IMqConsumer<T extends Serializable> {
    /**
     * 消费者业务id
     *
     * @return 归属的业务域标识，不为空
     */
    String businessId();

    /**
     * 消费者id
     *
     * @return 消费台账行的身份键组成部分，同一负载类型内必须全局唯一，不为空
     */
    Integer consumeId();

    /**
     * 消费业务负载
     * <p>
     * 由上下文在消息级单飞保护与 PROCESSING 状态内调用；实现必须幂等（同一条消息可能因重试被多次投递）。
     * 失败时抛 {@code MqConsumeException} 并用 retryFlag 声明是否允许按记录重试；
     * 不得在消费内做任何厂商侧确认动作。
     * </p>
     *
     * @param message 业务负载
     * @throws MqConsumeException 消费失败
     */
    void consume(T message) throws MqConsumeException;

}
