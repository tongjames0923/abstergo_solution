package com.tbs.mq.v2.core;

import com.tbs.mq.v2.vo.MqConsumeVO;
import com.tbs.mq.v2.vo.MqMessageIncomeVO;

import java.io.Serializable;
import java.util.List;

/**
 * 消息分发处理器接口（v2 记录驱动模型 · 分发段）
 * <p>
 * 把一条已建档消息广播给该负载类型注册的全部消费者，产出逐消费者的台账行（{@link MqConsumeVO}）
 * 交由上下文经 {@link IMqRecorder} 落库。本接口不做厂商确认、不推进主记录状态，只负责消费与结果收集。
 * </p>
 *
 * @param <R> 厂商原始消息类型（传输层）
 * @param <T> 业务负载类型
 */
public interface IMqMessageProcessor<R, T extends Serializable> {

    /**
     * 分发并消费消息
     *
     * @param context 当前消费上下文，提供负载类型用于回查消费者
     * @param message 已建档的消息主记录VO
     * @return 逐消费者的台账行（SUCCESS/FAIL）；message 为 null 时返回空集合
     */
    List<MqConsumeVO<T>> process(IMqContext<R, T> context, MqMessageIncomeVO<R, T> message);

}
