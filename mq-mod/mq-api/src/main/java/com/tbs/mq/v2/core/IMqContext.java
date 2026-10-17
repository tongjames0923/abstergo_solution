package com.tbs.mq.v2.core;

import com.tbs.mq.v2.vo.MqMessageVO;

import java.io.Serializable;
import java.util.List;

/**
 * MQ消费上下文接口
 * <p>
 * 一个上下文实例对应一个队列/一个消费环境，职责详见 {@link AbstractMqContext} 的链路说明。
 * 业务消费由注册到 {@code IConsumerRegistry} 的 {@code IMqConsumer} 承担，上下文不再提供消费钩子。
 * </p>
 *
 * @param <T> 厂商原始消息类型
 */
public interface IMqContext<R, T extends Serializable> {

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
     * 业务消费钩子，由处理线程在单飞保护内调用
     *
     * @param message 厂商原始消息
     */
    void handleMessage(MqMessageVO<R,T> message);

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
     * @return 厂商原始消息类型
     */
    Class<R> getMessageType();

    Class<T> getMessagePayloadType();

}
