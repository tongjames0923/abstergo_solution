package com.tbs.mq.v2.core;

import com.tbs.mq.v2.enums.MqMessageStatusEnum;
import com.tbs.mq.v2.vo.MqMessageVO;
import com.tbs.mq.v2.vo.MqRecordVO;

import java.io.Serializable;
import java.util.List;

/**
 * 消息记录器接口
 * <p>
 * v2 记录驱动模型的可靠性真源：所有消费状态以该接口的落库记录为准，
 * 重试、留痕、恢复均基于记录推进，不依赖厂商死信/重投能力。
 * 实现类仅保留存取能力，不得内嵌调度/重投逻辑。
 * </p>
 *
 * @param <T> 厂商原始消息类型
 */
public interface IMqRecorder<T extends Serializable> {
    /**
     * 批量记录消息
     * <p>
     * 返回逐条建档结果，结果项通过 messageId 与入参配对；实现方应为每条入参都返回对应结果项，
     * 上下文侧对配对缺席的条目一律按建档失败处理（宁拒勿漏）。
     * </p>
     *
     * @param messages 待建档的消息VO批次，不为空
     * @return 逐条建档结果列表，支持部分成功
     */
    List<MqRecordVO<T>> record(List<MqMessageVO<T>> messages);

    /**
     * 更新消息状态
     *
     * @param messageId 消息ID
     * @param flag      目标状态
     */
    void updateFlag(String messageId, MqMessageStatusEnum flag);

    /**
     * 更新消息状态为错误，并留痕失败原因供重试扫描与人工排查
     *
     * @param messageId 消息ID
     * @param e         失败原因异常
     */
    void errorFlag(String messageId, Exception e);

}
