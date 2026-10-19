package com.tbs.mq.v2.core;

import com.tbs.common.function.SConsumer;
import com.tbs.mq.v2.vo.MqMessageIncomeVO;

import java.io.Serializable;
import java.util.List;

/**
 * 消息接收者接口（v2 记录驱动模型 · 接收段）
 * <p>
 * 承接厂商监听回调的建档与确认编排：组装主记录VO → 批量建档 → 逐条结果按 messageId 配对，
 * 通过回调区分入队成功/失败，使接收段与厂商 ack/nak 语义解耦。详见 {@link AbstractMqMessageReceiver}。
 * </p>
 *
 * @param <R> 厂商原始消息类型（传输层）
 * @param <T> 业务负载类型
 */
public interface IMqMessageReceiver<R, T extends Serializable> {

    /**
     * 接收并建档一批厂商消息
     *
     * @param context          接收建档归属的消费上下文（队列级作用域）
     * @param messages         厂商原始消息批次，null 元素被忽略
     * @param onInqueueSuccess 建档成功并入内存待处理队列后的回调（一般为厂商 ack），可空
     * @param onInqueueFail    建档失败或结果缺席后的回调（一般为厂商 nak），可空
     * @return 建档成功、可进入处理链路的消息主记录VO列表
     */
    List<MqMessageIncomeVO<R, T>> receive(IMqContext<R, T> context, List<R> messages, SConsumer<R> onInqueueSuccess,
        SConsumer<R> onInqueueFail);

}
