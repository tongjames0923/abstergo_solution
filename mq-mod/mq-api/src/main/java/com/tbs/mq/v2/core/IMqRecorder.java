package com.tbs.mq.v2.core;

import com.tbs.mq.v2.enums.MqMessageStatusEnum;
import com.tbs.mq.v2.vo.MqConsumeVO;
import com.tbs.mq.v2.vo.MqMessageVO;
import com.tbs.mq.v2.vo.MqRecordVO;

import java.io.Serializable;
import java.util.List;

/**
 * 消息记录器接口（v2 记录驱动模型 · 可靠性真源）
 * <p>
 * 统一承载两层记录：消息主记录（{@link #record}/{@link #updateFlag}/{@link #errorFlag}/{@link #get}）
 * 管“消息有没有进体系”，消费台账（{@link #recordConsume}）管“每个消费者有没有消费成功”；
 * 所有重试、恢复均以该接口的落库记录为准，不依赖厂商死信/重投能力。
 * {@link #beginTransaction(IMqContext)}/{@link #endTransaction(IMqContext, int)} 供处理线程把“状态推进 + 台账落库 + 终态留痕”纳入同一事务。
 * 除按全局唯一 messageId 回查的 {@link #get(String)} 外，其余方法均以 {@link IMqContext} 为首参，使同一记录器实例可按上下文（队列/环境）隔离归属；
 * 实现类仅保留存取能力，不得内嵌调度/重投逻辑。
 * </p>
 *
 * @param <R> 厂商原始消息类型（传输层）
 * @param <T> 业务负载类型
 */
public interface IMqRecorder<R, T extends Serializable> {

    /**
     * 获取记录器ID
     * <p>
     * 记录器实例的身份标识，供多记录器共存或按上下文绑定记录器时寻址使用。
     * </p>
     *
     * @return 记录器ID，全局唯一
     */
    Long getRecorderId();

    /**
     * 批量记录消息
     * <p>
     * 返回逐条建档结果，结果项通过 messageId 与入参配对；实现方应为每条入参都返回对应结果项，
     * 上下文侧对配对缺席的条目一律按建档失败处理（宁拒勿漏）。
     * </p>
     *
     * @param context  建档归属的消费上下文（队列级作用域）
     * @param messages 待建档的消息VO批次，不为空
     * @return 逐条建档结果列表，支持部分成功
     */
    List<MqRecordVO<T>> record(IMqContext<R, T> context, List<MqMessageVO<R, T>> messages);

    /**
     * 开始事务
     *
     * @param context 当前上下文
     * @return 事务ID
     */
    default int beginTransaction(IMqContext<R, T> context) {
        return 0;
    }

    /**
     * 结束事务
     *
     * @param context       当前上下文
     * @param transactionId 事务ID
     */
    default void endTransaction(IMqContext<R, T> context, int transactionId) {
        return;
    }


    /**
     * 更新消息主记录状态
     *
     * @param context   当前上下文
     * @param messageId 消息ID
     * @param flag      目标状态
     */
    void updateFlag(IMqContext<R, T> context, String messageId, MqMessageStatusEnum flag);

    /**
     * 更新消息状态为错误，并留痕失败原因供重试扫描与人工排查
     *
     * @param context   当前上下文
     * @param messageId 消息ID
     * @param e         失败原因异常
     */
    void errorFlag(IMqContext<R, T> context, String messageId, Exception e);

    /**
     * 批量记录消费台账
     * <p>
     * 一次分发后同一消息下各消费者的结果批处理落库，支持成否混合（SUCCESS/FAIL 同行表不互斥）。
     * </p>
     *
     * @param context 当前上下文
     * @param record  消费台账批次，不为空
     * @return 落库成功的行数
     */
    int recordConsume(IMqContext<R, T> context, List<MqConsumeVO<T>> record);


    /**
     * 按消息ID回查消息主记录
     * <p>
     * 重试扫描/台账回灌场景取回 payload 与原始消息的入口；messageId 全局唯一，故本方法不带上下文参数。
     * </p>
     *
     * @param messageId 消息ID
     * @param context   当前上下文
     * @return 消息主记录VO，不存在时由实现方决定返回空或抛异常
     */
    MqMessageVO<R, T> get(IMqContext<R, T> context, String messageId);

}
