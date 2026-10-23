package com.tbs.mq.v2.core;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.io.Serializable;
import java.util.List;

/**
 * MQ消费上下文接口
 * <p>
 * 一个上下文实例对应一个队列/一个消费环境，职责详见 {@link AbstractMqContext} 的链路说明。
 * 上下文负责建档、分发与台账推进；业务消费由注册到 {@code IConsumerRegistry} 的 {@code IMqConsumer} 承担。
 * 上下文同时作为作用域身份（{@link #getContextId()}）传入接收/记录/抢占层，使这些协作 Bean 可按队列隔离归属；
 * 业务消费者仍不感知上下文（{@code consume} 只接收负载）。
 * </p>
 *
 * @param <R> 厂商原始消息类型（传输层）
 * @param <T> 业务负载类型（消费者面对的类型）
 */
public interface IMqContext<R, T extends Serializable> extends InitializingBean, DisposableBean {

    /**
     * 获取上下文ID
     * <p>
     * 上下文（队列/消费环境）的身份标识；记录层以此隔离不同队列的消息与台账归属，
     * 使同一组协作 Bean 可服务多个上下文而不串数据。
     * </p>
     *
     * @return 上下文ID，全局唯一
     */
    Long getContextId();

    /**
     * 消息接收处理
     * <p>
     * 厂商监听回调入口，批量接收并建档；建档成功才允许进入后续处理与重试链路。
     * </p>
     *
     * @param messages 厂商原始消息批次
     */
    void messageReceived(List<R> messages);

    /**
     * 结清厂商侧投递确认
     * <p>
     * 仅在消息建档成功后调用；具体确认语义由厂商子类实现。
     * </p>
     *
     * @param message 厂商原始消息
     */
    void ackMessage(R message);

    /**
     * 拒收厂商侧投递
     * <p>
     * 建档失败时的兜底路径，全链路唯一依赖厂商能力的位置；每条消息至多调用一次。
     * </p>
     *
     * @param message 厂商原始消息
     */
    void nakMessage(R message);

    /**
     * 获取消息类型
     *
     * @return 厂商原始消息类型（传输层 R）
     */
    Class<R> getMessageType();

    /**
     * 获取业务负载类型
     * <p>
     * 消费者注册与分发回查的键：{@code IConsumerRegistry} 按该类型路由到对应的 {@code IMqConsumer} 集合。
     * </p>
     *
     * @return 业务负载类型（消费者面对的类型 T）
     */
    Class<T> getMessagePayloadType();

    /**
     * 获取队列大小,当前队列中待消费消息数量
     *
     * @return 队列大小
     */
    Integer getQueueSize();


}
