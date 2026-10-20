package com.tbs.rabbitmq.v2;

import com.tbs.mq.v2.consume.IConsumerRegistry;
import com.tbs.mq.v2.core.AbstractMqMessageProcessor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;

import java.io.Serializable;

/**
 * RabbitMQ 消息分发器（v2 记录驱动模型 · 分发段的 RabbitMQ 落地）
 * <p>
 * 分发逻辑（按负载类型回查消费者、广播消费、逐条构建台账）与厂商无关，全部由父类
 * {@link AbstractMqMessageProcessor} 实现；本类仅把传输层固定为 Spring AMQP 的 {@link Message}，
 * 以便注入到 {@code R=Message} 的消费上下文，并对接容器内的 {@link IConsumerRegistry}。
 * </p>
 * <p>
 * 抽象类，业务子类绑定具体负载类型 T 并声明为 Spring Bean 即可。
 * </p>
 *
 * @param <T> 业务负载类型（消费者面对的类型）
 * @author tongj
 * @since 2.0.0
 */
@Slf4j
public abstract class RabbitMqMessageProcessor<T extends Serializable> extends AbstractMqMessageProcessor<Message, T> {

    /**
     * 消费者注册器，按负载类型路由到对应的 {@code IMqConsumer} 集合
     */
    @Resource
    private IConsumerRegistry consumerRegistry;

    @Override
    protected IConsumerRegistry getConsumerRegistry() {
        return consumerRegistry;
    }
}
